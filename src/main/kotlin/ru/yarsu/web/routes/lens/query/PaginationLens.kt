package ru.yarsu.web.routes.lens.query

import org.http4k.lens.LensFailure
import org.http4k.lens.Query
import org.http4k.lens.composite
import org.http4k.lens.int

data class PaginationParameters(
    val page: Int,
    val recordsPerPage: Int,
)

val paginationLens =
    Query.composite { req ->
        val page =
            Query
                .int()
                .map(
                    { number ->
                        if (number < 1) {
                            throw LensFailure(
                                message =
                                    "Некорректное значение параметра page. " +
                                        "Ожидается натуральное число, но получено $number",
                            )
                        }
                        number
                    },
                    { it },
                ).defaulted("page", 1)(req)

        val recordsPerPage =
            Query
                .int()
                .map(
                    { number ->
                        val allowed = listOf(5, 10, 20, 50)
                        if (number !in allowed) {
                            throw LensFailure(
                                message =
                                    "Некорректное значение параметра records-per-page. " +
                                        "Допустимые значения: $allowed. Получено $number",
                            )
                        }
                        number
                    },
                    { it },
                ).defaulted("records-per-page", 10)(req)

        PaginationParameters(page, recordsPerPage)
    }
