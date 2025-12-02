package ru.yarsu.storage

import ru.yarsu.data.Shipment
import ru.yarsu.internal.SwgType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ShipmentStorage(
    initialShipments: List<Shipment> = emptyList(),
) {
    private val shipmentList = initialShipments.toMutableList()

    fun getAllShipments(): List<Shipment> = shipmentList.toList()

    fun getShipmentById(id: UUID?): Shipment? = shipmentList.find { it.id == id }

    fun getShipmentsBySwgType(swgType: SwgType?): List<Shipment> = shipmentList.filter { it.swg == swgType }

    fun getShipmentsByManager(managerId: UUID): List<Shipment> = shipmentList.filter { it.manager == managerId }

    fun getShipmentsByDate(date: LocalDate): List<Shipment> = shipmentList.filter { it.shipmentDateTime.toLocalDate() == date }

    fun getShipmentsWithMinCost(minCost: BigDecimal): List<Shipment> = shipmentList.filter { it.cost >= minCost }

    fun hasShipmentsByManager(managerId: UUID): Boolean = shipmentList.any { it.manager == managerId }

    fun getShipmentsByWashing(washing: Boolean): List<Shipment> = shipmentList.filter { it.washing == washing }

    fun getShipmentsByPeriod(
        from: LocalDate,
        to: LocalDate,
    ): List<Shipment> =
        shipmentList.filter { shipment ->
            val shipmentDate = shipment.shipmentDateTime.toLocalDate()
            shipmentDate >= from && shipmentDate <= to
        }

    fun getShipmentsByMonth(year: Int): Map<Int, List<Shipment>> =
        shipmentList.filter { it.shipmentDateTime.year == year }.groupBy { it.shipmentDateTime.monthValue }

    fun getShipmentsByTruckId(truckId: UUID): List<Shipment> = shipmentList.filter { it.dumpTruck == truckId }

    fun addShipment(
        title: String,
        swg: SwgType,
        measure: String,
        count: BigDecimal,
        price: BigDecimal,
        cost: BigDecimal,
        shipmentDateTime: LocalDateTime,
        washing: Boolean,
        dumpTruckId: UUID,
        managerId: UUID,
    ): UUID {
        val newShipment =
            Shipment(
                id = UUID.randomUUID(),
                title = title,
                swg = swg,
                measure = measure,
                count = count,
                price = price,
                cost = cost,
                shipmentDateTime = shipmentDateTime,
                dumpTruck = dumpTruckId,
                washing = washing,
                manager = managerId,
            )
        shipmentList.add(newShipment)
        return newShipment.id
    }

    fun updateShipment(
        id: UUID,
        title: String,
        swg: SwgType,
        measure: String,
        count: BigDecimal,
        price: BigDecimal,
        cost: BigDecimal,
        shipmentDateTime: LocalDateTime,
        washing: Boolean,
        dumpTruck: UUID,
        manager: UUID,
    ): Boolean {
        val index = shipmentList.indexOfFirst { it.id == id }
        if (index == -1) return false

        val updatedShipment =
            Shipment(
                id = id,
                title = title,
                swg = swg,
                measure = measure,
                count = count,
                price = price,
                cost = cost,
                shipmentDateTime = shipmentDateTime,
                dumpTruck = dumpTruck,
                washing = washing,
                manager = manager,
            )

        shipmentList[index] = updatedShipment
        return true
    }

    fun deleteShipment(id: UUID): Boolean = shipmentList.removeIf { it.id == id }

    fun hasShipmentsByDumpTruck(dumpTruckId: UUID): Boolean = shipmentList.any { it.dumpTruck == dumpTruckId }

    fun updateShipmentTitle(id: UUID, newTitle: String): Boolean {
        val index = shipmentList.indexOfFirst { it.id == id }
        if (index == -1) return false

        val existingShipment = shipmentList[index]
        val updatedShipment = existingShipment.copy(title = newTitle)

        shipmentList[index] = updatedShipment
        return true
    }
}
