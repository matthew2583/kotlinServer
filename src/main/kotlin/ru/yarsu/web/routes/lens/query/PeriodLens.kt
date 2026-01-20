package ru.yarsu.web.routes.lens.query

import org.http4k.lens.LensFailure
import org.http4k.lens.Query
import org.http4k.lens.composite
import org.http4k.lens.string
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class PeriodLens(
    val from: LocalDate,
    val to: LocalDate,
)

val periodLens =
    Query.composite { req ->
        val from =
            Query
                .string()
                .map(
                    { str ->
                        try {
                            LocalDate.parse(str)
                        } catch (e: DateTimeParseException) {
                            throw LensFailure(
                                message =
                                    "Некорректное значение параметра from. Ожидается дата (YYYY-MM-DD), " +
                                        "получено: $str",
                                cause = e,
                            )
                        }
                    },
                    { it.toString() },
                ).required("from")(req)

        val to =
            Query
                .string()
                .map(
                    { str ->
                        try {
                            LocalDate.parse(str)
                        } catch (e: DateTimeParseException) {
                            throw LensFailure(
                                message =
                                    "Некорректное значение параметра to. Ожидается дата (YYYY-MM-DD), " +
                                        "получено: $str",
                                cause = e,
                            )
                        }
                    },
                    { it.toString() },
                ).required("to")(req)

        if (from.isAfter(to)) {
            throw LensFailure(
                message = "Дата 'from' должна быть раньше или равна дате 'to'",
                cause = null,
            )
        }

        PeriodLens(from, to)
    }
