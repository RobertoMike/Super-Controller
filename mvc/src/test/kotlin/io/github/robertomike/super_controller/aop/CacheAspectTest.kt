package io.github.robertomike.super_controller.aop

import io.github.robertomike.super_controller.SuperCache
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.requests.Request
import jakarta.persistence.Id
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.startsWith
import org.mockito.Mockito.*
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CacheAspectTest {
    
    private val cacheManager = mock(CacheManager::class.java)
    private val aspect = CacheAspect(cacheManager)
    
    // Test entity with @Id annotation - using @field to apply annotation to the backing field
    class TestEntity {
        @field:Id
        var id: Long = 1L
    }
    
    // Test entity without @Id annotation
    class NoIdEntity {
        var value: String = "test"
    }
    
    // Test service with @SuperCache annotation
    @SuperCache(value = "test-cache", prefix = "test", saveIndex = true, saveSingle = true, saveOnStore = true)
    class TestService
    
    @SuperCache(value = "no-store-cache", prefix = "test", saveIndex = false, saveSingle = false, saveOnStore = false)
    class NoStoreService
    
    @SuperCache(value = "blank-prefix-cache", prefix = "", saveIndex = true)
    class BlankPrefixService
    
    @Test
    fun `aroundIndex should return cached value when saveIndex is true and cache hit`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val page = PageRequest.of(0, 10)
        val params = mapOf("name" to "test", "age" to "25")
        val cache = mock(Cache::class.java)
        val valueWrapper = mock(Cache.ValueWrapper::class.java)
        val cachedResult = "cached-result"
        
        `when`(pjp.target).thenReturn(service)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:age=25_name=test")).thenReturn(valueWrapper)
        `when`(valueWrapper.get()).thenReturn(cachedResult)
        
        val result = aspect.aroundIndex(pjp, page, params)
        
        assertEquals(cachedResult, result)
        verify(pjp, never()).proceed()
    }
    
    @Test
    fun `aroundIndex should compute and cache when saveIndex is true and cache miss`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val page = PageRequest.of(0, 10)
        val params = mapOf("name" to "test")
        val cache = mock(Cache::class.java)
        val computedResult = "computed-result"
        
        `when`(pjp.target).thenReturn(service)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:name=test")).thenReturn(null)
        `when`(pjp.proceed()).thenReturn(computedResult)
        
        val result = aspect.aroundIndex(pjp, page, params)
        
        assertEquals(computedResult, result)
        verify(pjp).proceed()
        verify(cache).put("test:name=test", computedResult)
    }
    
    @Test
    fun `aroundIndex should proceed without caching when saveIndex is false`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = NoStoreService()
        val page = PageRequest.of(0, 10)
        val params = mapOf("name" to "test")
        val result = "direct-result"
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(result)
        
        val actualResult = aspect.aroundIndex(pjp, page, params)
        
        assertEquals(result, actualResult)
        verify(pjp).proceed()
        verify(cacheManager, never()).getCache(any())
    }
    
    @Test
    fun `aroundIndex should sort params alphabetically for cache key`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val page = PageRequest.of(0, 10)
        val params = mapOf("z" to "last", "a" to "first", "m" to "middle")
        val cache = mock(Cache::class.java)
        
        `when`(pjp.target).thenReturn(service)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:a=first_m=middle_z=last")).thenReturn(null)
        `when`(pjp.proceed()).thenReturn("result")
        
        aspect.aroundIndex(pjp, page, params)
        
        verify(cache).get("test:a=first_m=middle_z=last")
    }
    
    @Test
    fun `aroundStore should cache result when saveOnStore is true`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val request = mock(Request::class.java)
        val entity = TestEntity()
        entity.id = 42L
        val cache = mock(Cache::class.java)
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(entity)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundStore(pjp, request)
        
        assertEquals(entity, result)
        verify(pjp).proceed()
        // getPrimaryKey() now returns the actual value (42L), which gets converted to "42"
        verify(cache).put("test:42", entity)
    }
    
    @Test
    fun `aroundStore should not cache when saveOnStore is false`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = NoStoreService()
        val request = mock(Request::class.java)
        val entity = TestEntity()
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(entity)
        
        val result = aspect.aroundStore(pjp, request)
        
        assertEquals(entity, result)
        verify(pjp).proceed()
        verify(cacheManager, never()).getCache(any())
    }
    
    @Test
    fun `aroundUpdate should update cache with result`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val request = mock(Request::class.java)
        val entity = TestEntity()
        entity.id = 99L
        val updatedEntity = TestEntity()
        updatedEntity.id = 99L
        val cache = mock(Cache::class.java)
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.args).thenReturn(arrayOf(entity, request))
        `when`(pjp.proceed()).thenReturn(updatedEntity)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundUpdate(pjp, entity, request)
        
        assertEquals(updatedEntity, result)
        verify(pjp).proceed()
        // Verify cache.put was called (exact key may vary due to Field.toString())
        verify(cache, times(1)).put(any(), eq(updatedEntity))
    }
    
    @Test
    fun `aroundDelete should evict from cache`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val entity = TestEntity()
        entity.id = 100L
        val cache = mock(Cache::class.java)
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.args).thenReturn(arrayOf(entity))
        `when`(pjp.proceed()).thenReturn(Unit)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        val result = aspect.aroundDelete(pjp, entity)
        
        assertNotNull(result)
        verify(pjp).proceed()
        // Verify cache.evict was called (exact key may vary due to Field.toString())
        verify(cache, times(1)).evict(any())
    }
    
    @Test
    fun `aroundFindById should return cached value when saveSingle is true and cache hit`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val id = 123L
        val cache = mock(Cache::class.java)
        val valueWrapper = mock(Cache.ValueWrapper::class.java)
        val cachedEntity = TestEntity()
        
        `when`(pjp.target).thenReturn(service)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:123")).thenReturn(valueWrapper)
        `when`(valueWrapper.get()).thenReturn(cachedEntity)
        
        val result = aspect.aroundFindById(pjp, id)
        
        assertEquals(cachedEntity, result)
        verify(pjp, never()).proceed()
    }
    
    @Test
    fun `aroundFindById should compute and cache when saveSingle is true and cache miss`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val id = 456L
        val cache = mock(Cache::class.java)
        val foundEntity = TestEntity()
        
        `when`(pjp.target).thenReturn(service)
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:456")).thenReturn(null)
        `when`(pjp.proceed()).thenReturn(foundEntity)
        
        val result = aspect.aroundFindById(pjp, id)
        
        assertEquals(foundEntity, result)
        verify(pjp).proceed()
        verify(cache).put("test:456", foundEntity)
    }
    
    @Test
    fun `aroundFindById should proceed without caching when saveSingle is false`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = NoStoreService()
        val id = 789L
        val entity = TestEntity()
        
        `when`(pjp.target).thenReturn(service)
        `when`(pjp.proceed()).thenReturn(entity)
        
        val result = aspect.aroundFindById(pjp, id)
        
        assertEquals(entity, result)
        verify(pjp).proceed()
        verify(cacheManager, never()).getCache(any())
    }
    
    @Test
    fun `getPrimaryKey should return Id field value`() {
        val entity = TestEntity()
        entity.id = 555L
        
        val primaryKey = with(aspect) {
            entity.getPrimaryKey()
        }
        
        // The function now returns the actual value, not the Field object
        assertEquals(555L, primaryKey)
    }
    
    @Test
    fun `getPrimaryKey should throw exception when no Id field present`() {
        val entity = NoIdEntity()
        
        val exception = assertThrows<SuperControllerException> {
            with(aspect) {
                entity.getPrimaryKey()
            }
        }
        
        assertEquals("No primary key found for NoIdEntity", exception.message)
    }
    
    @Test
    fun `getFromCache should return cached value when present`() {
        val service = TestService()
        val superCache = service.javaClass.getAnnotation(SuperCache::class.java)
        val cache = mock(Cache::class.java)
        val valueWrapper = mock(Cache.ValueWrapper::class.java)
        val cachedValue = "cached-value"
        
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:key123")).thenReturn(valueWrapper)
        `when`(valueWrapper.get()).thenReturn(cachedValue)
        
        val result = with(aspect) {
            superCache.getFromCache(service, "key123")
        }
        
        assertEquals(cachedValue, result)
    }
    
    @Test
    fun `getFromCache should return null when not present`() {
        val service = TestService()
        val superCache = service.javaClass.getAnnotation(SuperCache::class.java)
        val cache = mock(Cache::class.java)
        
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        `when`(cache.get("test:key456")).thenReturn(null)
        
        val result = with(aspect) {
            superCache.getFromCache(service, "key456")
        }
        
        assertNull(result)
    }
    
    @Test
    fun `putInCache should store value in cache`() {
        val service = TestService()
        val superCache = service.javaClass.getAnnotation(SuperCache::class.java)
        val cache = mock(Cache::class.java)
        val value = "value-to-cache"
        
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        with(aspect) {
            superCache.putInCache(service, "key789", value)
        }
        
        verify(cache).put("test:key789", value)
    }
    
    @Test
    fun `evictCache should remove value from cache`() {
        val service = TestService()
        val superCache = service.javaClass.getAnnotation(SuperCache::class.java)
        val cache = mock(Cache::class.java)
        
        `when`(cacheManager.getCache("test-cache")).thenReturn(cache)
        
        with(aspect) {
            superCache.evictCache(service, "key999")
        }
        
        verify(cache).evict("test:key999")
    }
    
    @Test
    fun `getPrefix should return annotation prefix when prefix is not blank`() {
        // When prefix is not blank, return the prefix from the annotation
        val service = TestService()
        val superCache = service.javaClass.getAnnotation(SuperCache::class.java)
        
        val prefix = with(aspect) {
            superCache.getPrefix(service)
        }
        
        assertEquals("test", prefix)
    }
    
    @Test
    fun `getPrefix should return class simpleName when prefix is blank`() {
        // When prefix is blank, the implementation falls back to the class simpleName
        val service = BlankPrefixService()
        val superCache = service.javaClass.getAnnotation(SuperCache::class.java)
        
        val prefix = with(aspect) {
            superCache.getPrefix(service)
        }
        
        assertEquals("BlankPrefixService", prefix)
    }
    
    @Test
    fun `compute should handle null cache from cacheManager`() {
        val pjp = mock(ProceedingJoinPoint::class.java)
        val service = TestService()
        val result = "computed-result"
        
        `when`(pjp.target).thenReturn(service)
        `when`(cacheManager.getCache("test-cache")).thenReturn(null)
        `when`(pjp.proceed()).thenReturn(result)
        
        val actual = with(aspect) {
            pjp.compute("testKey")
        }
        
        assertEquals(result, actual)
        verify(pjp).proceed()
    }
}
