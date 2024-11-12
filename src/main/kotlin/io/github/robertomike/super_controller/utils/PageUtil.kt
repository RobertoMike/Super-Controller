package io.github.robertomike.super_controller.utils

import io.github.robertomike.baradum.Baradum
import io.github.robertomike.hefesto.models.BaseModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

object PageUtil {
    fun <T : BaseModel> transformHefestoPage(query: Baradum<T>, pageable: PageRequest): Page<T> {
        val page: io.github.robertomike.hefesto.utils.Page<T> =
            query.page(pageable.pageSize, pageable.pageNumber.toLong() * pageable.pageSize)

        return PageImpl(page.data, pageable, page.total)
    }
}
