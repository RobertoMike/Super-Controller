package io.github.robertomike.super_controller.services

import io.github.robertomike.baradum.Baradum
import io.github.robertomike.baradum.filters.Filter
import io.github.robertomike.hefesto.actions.JoinFetch
import io.github.robertomike.hefesto.models.BaseModel
import io.github.robertomike.super_controller.exceptions.NotFoundException
import io.github.robertomike.super_controller.exceptions.SuperControllerException
import io.github.robertomike.super_controller.requests.Request
import io.github.robertomike.super_controller.utils.ClassUtils
import io.github.robertomike.super_controller.utils.PageUtil
import jakarta.annotation.PostConstruct
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository
import java.util.*

abstract class SuperService<M : BaseModel, ID> : ClassUtils, BasicService<M, ID> {
    @Autowired
    lateinit var modelMapper: ModelMapper

    var useCustomFindById = false
    var useCustomSave = false
    var useCustomDelete = false

    @PostConstruct
    private fun init() {
        config()
    }

    open fun config() {
    }

    open fun filters(): List<Filter<*>> {
        return listOf()
    }

    open fun with(): List<JoinFetch> {
        return listOf()
    }

    open fun beforeIndex(queryBuilder: Baradum<M>?) {
    }

    override fun index(page: PageRequest): Page<M> {
        val queryBuilder = Baradum.make(model)
            .allowedFilters(filters())
            .builder { it.with(*with().toTypedArray()) }

        beforeIndex(queryBuilder)

        val models = PageUtil.transformHefestoPage(queryBuilder, page)

        afterIndex(models)

        return models
    }

    open fun afterIndex(models: Page<M>?) {
    }

    open fun beforeStore(model: M, request: Request) {
    }

    override fun store(request: Request): M {
        val model = newModel()

        beforeStore(model, request)

        mappingStore(model, request)

        save(model)

        afterStore(model, request)

        return model
    }

    open fun afterStore(model: M, request: Request) {
    }

    open fun beforeShow(id: ID) {
    }

    override fun show(id: ID): M {
        beforeShow(id)

        val model = searchModelById(id)

        afterShow(model)

        return model
    }

    open fun afterShow(model: M) {
    }

    open fun beforeUpdate(model: M, request: Request) {
    }

    override fun update(id: ID, request: Request): M {
        val model = searchModelById(id)

        beforeUpdate(model, request)

        mappingUpdate(model, request)

        save(model)

        afterUpdate(model, request)

        return model
    }

    protected open fun mappingStore(model: M, request: Request) {
        modelMapper.map(request, model)
    }

    protected open fun mappingUpdate(model: M, request: Request) {
        modelMapper.map(request, model)
    }

    open fun afterUpdate(model: M, request: Request) {
    }

    open fun beforeDelete(id: ID) {
    }

    override fun delete(id: ID) {
        beforeDelete(id)

        val model = searchModelById(id)

        delete(model)

        afterDelete(model)
    }

    open fun afterDelete(model: M) {
    }

    protected open fun save(model: M) {
        if (useCustomSave) {
            customSave(model)
            return
        }

        val repository = repository
        if (repository is CrudRepository<M, ID>) {
            repository.save(model)
            return
        }

        throw SuperControllerException(
            "The repository doesn't extend from CrudRepository and customSave is not active"
        )
    }

    protected open fun delete(model: M) {
        if (useCustomDelete) {
            customDelete(model)
            return
        }

        val repository = repository
        if (repository is CrudRepository<M, ID>) {
            repository.delete(model)
            return
        }

        throw SuperControllerException(
            "The repository doesn't extend from CrudRepository and customDelete is not active"
        )
    }

    protected open fun searchModelById(id: ID): M {
        val repository = repository

        val model = when {
            useCustomFindById -> customFindById(id)
            repository is CrudRepository<M, ID> -> repository.findById(id)
            else -> throw SuperControllerException(
                "The repository doesn't extend from CrudRepository and customFindById is not active"
            )
        }

        return model.orElseThrow {
            NotFoundException(
                "Cannot find model with id $id"
            )
        }
    }

    open fun customSave(model: M) {
        throw SuperControllerException(
            "Cannot save. The service doesn't override customSave"
        )
    }

    open fun customDelete(model: M) {
        throw SuperControllerException(
            "Cannot delete. The service doesn't override customDelete"
        )
    }

    open fun customFindById(id: ID): Optional<M> {
        throw SuperControllerException(
            "The service doesn't override customFindById"
        )
    }

    protected open fun newModel(): M {
        val constructor = model.constructors.first { c -> c.parameterCount == 0 }
        return try {
            if (constructor == null) {
                model.getConstructor().newInstance()
            }
            constructor.newInstance() as M
        } catch (e: Exception) {
            throw SuperControllerException("Can't create new model instance", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val model: Class<M>
        get() = generics[0] as Class<M>

    open val repository: Repository<M, ID>
        get() {
            throw SuperControllerException("Get repository not implemented")
        }
}
