package ru.yarsu.web.routes.util

import ru.yarsu.data.Shipment

object ResponseMappers {
    fun shipmentToBasicMap(shipment: Shipment) =
        mapOf(
            "Id" to shipment.id.toString(),
            "Title" to shipment.title,
            "Cost" to shipment.cost,
        )

    fun shipmentToBasicWithDateMap(shipment: Shipment) =
        mapOf(
            "Id" to shipment.id.toString(),
            "Title" to shipment.title,
            "ShipmentDate" to shipment.shipmentDateTime.toLocalDate().toString(),
            "Cost" to shipment.cost,
        )
}
