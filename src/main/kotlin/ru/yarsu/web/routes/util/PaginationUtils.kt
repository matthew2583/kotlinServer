package ru.yarsu.web.routes.util

object PaginationUtils {
    fun <T> applyPaging(
        items: List<T>,
        page: Int,
        perPage: Int,
    ): List<T> = items.drop((page - 1) * perPage).take(perPage)
}
