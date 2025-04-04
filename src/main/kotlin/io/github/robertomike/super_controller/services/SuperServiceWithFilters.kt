package io.github.robertomike.super_controller.services

import io.github.robertomike.baradum.Baradum
import io.github.robertomike.baradum.filters.Filter
import io.github.robertomike.hefesto.actions.JoinFetch
import io.github.robertomike.hefesto.models.BaseModel
import io.github.robertomike.super_controller.utils.PageUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

/**
 * Abstract class providing a basic implementation of a service with filters.
 *
 * This class provides a foundation for creating services that require filtering capabilities.
 * It includes methods for defining filters and joins, as well as a basic implementation of the index method.
 *
 * @param M The type of the model being managed by this service.
 * @param ID The type of the ID of the model being managed by this service.
 */
abstract class SuperServiceWithFilters<M : BaseModel, ID> : SuperService<M, ID>() {
    /**
     * Returns a list of filters to be applied to the service's queries.
     *
     * By default, this method returns an empty list. Subclasses can override this method to provide their own filters.
     *
     * @return A list of filters to be applied to the service's queries.
     */
    open fun filters(): List<Filter<*>> {
        return listOf()
    }

    /**
     * Returns a list of joins to be applied to the service's queries.
     *
     * By default, this method returns an empty list. Subclasses can override this method to provide their own joins.
     *
     * @return A list of joins to be applied to the service's queries.
     */
    open fun with(): List<JoinFetch> {
        return listOf()
    }

    /**
     * Called before the index method is executed, allowing subclasses to modify the query builder.
     *
     * By default, this method does nothing. Subclasses can override this method to perform any necessary actions before the index method is executed.
     *
     * @param queryBuilder The query builder to be modified.
     */
    open fun beforeIndex(queryBuilder: Baradum<M>) {
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
}
