package io.github.robertomike.super_controller.utils

import io.github.robertomike.baradum.Baradum
import io.github.robertomike.hefesto.models.BaseModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

/**
 * Utility class for working with pages of data.
 */
object PageUtil {
    /**
     * Transforms a page of data from a Hefesto query into a Spring Data page.
     *
     * @param query The Hefesto query to transform.
     * @param pageable The pageable request containing the page size and number.
     * @return A Spring Data page containing the transformed data.
     */
    fun <T : BaseModel> transformHefestoPage(query: Baradum<T>, pageable: PageRequest): Page<T> {
        val page: io.github.robertomike.hefesto.utils.Page<T> =
            query.page(pageable.pageSize, pageable.pageNumber.toLong() * pageable.pageSize)

        return PageImpl(page.data, pageable, page.total)
    }
}
