package ru.yarsu.web.routes.util

import ru.yarsu.data.Employees
import ru.yarsu.data.Shipment
import kotlin.collections.sortedWith
import kotlin.comparisons.thenBy

object SortingUtils {
    fun sortShipmentsByDateTime(shipments: List<Shipment>): List<Shipment> =
        shipments.sortedWith(compareBy({ it.shipmentDateTime }, { it.id }))

    fun sortShipmentsByDateTimeDesc(shipments: List<Shipment>): List<Shipment> =
        shipments.sortedWith(
            compareByDescending<Shipment> {
                it.shipmentDateTime
            }.thenBy { it.id },
        )

    fun sortEmployeesByName(employees: List<Employees>): List<Employees> =
        employees.sortedWith(
            compareBy(
                { it.name },
                { it.id },
            ),
        )
}
