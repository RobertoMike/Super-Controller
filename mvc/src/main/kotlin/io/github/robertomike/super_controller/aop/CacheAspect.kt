package io.github.robertomike.super_controller.aop

import io.github.robertomike.super_controller.SuperCache
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.requests.Request
import jakarta.persistence.Id
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.cache.CacheManager
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

const val CACHE_ANNOTATION = " && @target(io.github.robertomike.super_controller.SuperCache)"
const val BASIC_SERVICE = "io.github.robertomike.super_controller.services.interfaces.BasicService"

@Aspect
@Component
@ConditionalOnBean(CacheManager::class)
class CacheAspect(val cacheManager: CacheManager) {
    @Around(
        "execution(* ${BASIC_SERVICE}.index(..)) && args(page, params) $CACHE_ANNOTATION",
        argNames = "pjp,page,params"
    )
    fun aroundIndex(pjp: ProceedingJoinPoint, page: PageRequest, params: Map<String, String>): Any? {
        val superCache = pjp.getSuperCache()

        if (superCache.saveIndex) {
            val key = params.entries.sortedBy { it.key }
                .joinToString(separator = "_") { "${it.key}=${it.value}" }

            return pjp.compute(key)
        }

        return pjp.proceed()
    }

    @Around(
        "execution(* ${BASIC_SERVICE}.store(..)) && args(request) $CACHE_ANNOTATION",
        argNames = "pjp,request"
    )
    fun aroundStore(pjp: ProceedingJoinPoint, request: Request): Any? {
        val superCache = pjp.getSuperCache()

        val result = pjp.proceed()
        if (superCache.saveOnStore) {
            superCache.putInCache(pjp.target, result.getPrimaryKey().toString(), result)
        }

        return result
    }

    @Around(
        "execution(* ${BASIC_SERVICE}.update(..)) && args(model,request) $CACHE_ANNOTATION",
        argNames = "pjp,model,request"
    )
    fun aroundUpdate(pjp: ProceedingJoinPoint, model: Any, request: Request): Any? {
        val superCache = pjp.getSuperCache()

        val result = pjp.proceed()
        superCache.putInCache(pjp.target, model.getPrimaryKey().toString(), result)
        return result
    }

    @Around(
        "execution(* ${BASIC_SERVICE}.delete(..)) && args(model) $CACHE_ANNOTATION",
        argNames = "pjp,model"
    )
    fun aroundDelete(pjp: ProceedingJoinPoint, model: Any): Any? {
        val superCache = pjp.getSuperCache()

        val result = pjp.proceed()
        superCache.evictCache(pjp.target, model.getPrimaryKey().toString())
        return result
    }

    @Around(
        "execution(* ${BASIC_SERVICE}.findById(..)) && args(id) $CACHE_ANNOTATION"
    )
    fun aroundFindById(pjp: ProceedingJoinPoint, id: Any): Any? {
        if (pjp.getSuperCache().saveSingle) {
            return pjp.compute(id.toString())
        }

        return pjp.proceed()
    }

    fun ProceedingJoinPoint.getSuperCache(): SuperCache {
        return target.javaClass.getAnnotation(SuperCache::class.java)
    }

    fun ProceedingJoinPoint.compute(key: String): Any? {
        val superCache = getSuperCache()

        superCache.getFromCache(target, key)?.let {
            return it
        }

        val result = proceed()

        superCache.putInCache(target, key, result)

        return result
    }

    fun Any.getPrimaryKey(): Any {
        val field = this.javaClass.declaredFields
            .firstOrNull { it.isAnnotationPresent(Id::class.java) }
            ?: throw SuperControllerException("No primary key found for ${this.javaClass.simpleName}")
        
        field.isAccessible = true
        return field.get(this) ?: throw SuperControllerException("Primary key value is null for ${this.javaClass.simpleName}")
    }

    fun SuperCache.getFromCache(clazz: Any, key: String): Any? {
        return cacheManager.getCache(this.value)?.get(getPrefix(clazz) + ":" + key)?.get()
    }

    fun SuperCache.putInCache(clazz: Any, key: String, value: Any?) {
        cacheManager.getCache(this.value)?.put(getPrefix(clazz) + ":" + key, value)
    }

    fun SuperCache.evictCache(clazz: Any, key: String) {
        cacheManager.getCache(this.value)?.evict(getPrefix(clazz) + ":" + key)
    }

    fun SuperCache.getPrefix(clazz: Any): String {
        return this.prefix.ifBlank { clazz.javaClass.simpleName }
    }
}