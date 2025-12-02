package ru.yarsu.web.routes.lensValidate.query

import org.http4k.lens.LensFailure
import org.http4k.lens.Query
import org.http4k.lens.int
import java.time.Year

val yearLens =
    Query
        .int()
        .map(
            { year ->
                val currentYear = Year.now().value
                val minYear = 2000
                if (year < minYear || year > currentYear) {
                    throw LensFailure(
                        message = "Год должен быть в диапазоне от $minYear до $currentYear",
                        cause = null,
                    )
                }
                year
            },
            { it },
        ).required("year")
