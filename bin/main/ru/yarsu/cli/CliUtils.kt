package ru.yarsu.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ru.yarsu.data.Shipment

object CliUtils {
    private val mapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

    fun sortByDateTimeAsc(shipments: List<Shipment>) =
        shipments.sortedWith(
            compareBy({ it.shipmentDateTime }, { it.id }),
        )

    fun sortByDateTimeDesc(shipments: List<Shipment>) =
        shipments.sortedWith(
            compareByDescending<Shipment> { it.shipmentDateTime }.thenBy { it.id },
        )

    fun toJson(data: Any): String = mapper.writeValueAsString(data)
}
