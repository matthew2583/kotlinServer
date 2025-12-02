package ru.yarsu.web.routes.lensValidate.path

import org.http4k.lens.LensFailure
import org.http4k.lens.Path
import org.http4k.lens.string
import java.util.UUID

val shipmentIdLens =
    Path
        .string()
        .map(
            { str ->
                try {
                    UUID.fromString(str)
                } catch (e: IllegalArgumentException) {
                    throw LensFailure(
                        message =
                            "Некорректное значение переданного параметра shipment-id. Ожидается UUID, " +
                                "но получено текстовое значение",
                        cause = e,
                    )
                }
            },
            { it.toString() },
        ).of("shipment-id")
