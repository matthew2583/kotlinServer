package ru.yarsu.web.routes.lensValidate.query

import org.http4k.lens.LensFailure
import org.http4k.lens.Query
import org.http4k.lens.string
import ru.yarsu.internal.SwgType

val swgTypeLens =
    Query
        .string()
        .map(
            { type ->
                try {
                    SwgType.fromString(type)
                } catch (_: IllegalArgumentException) {
                    throw LensFailure(message = "Некорректное значение параметра by-swg-type: не соответствует типу ПГС")
                }
            },
            { it.toString() },
        ).required("by-swg-type")
