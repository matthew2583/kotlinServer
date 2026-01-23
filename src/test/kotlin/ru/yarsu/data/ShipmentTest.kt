package ru.yarsu.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.yarsu.domain.SwgType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ShipmentTest {
    @Test
    fun shipmentCreatedWithAllProperties() {
        val id = UUID.randomUUID()
        val truckId = UUID.randomUUID()
        val managerId = UUID.randomUUID()
        val dateTime = LocalDateTime.of(2024, 5, 15, 10, 30)

        val shipment =
            Shipment(
                id = id,
                title = "Test Shipment",
                swg = SwgType.RIVER_SAND,
                measure = "т",
                count = BigDecimal("100.50"),
                price = BigDecimal("500.00"),
                cost = BigDecimal("45.00"),
                shipmentDateTime = dateTime,
                dumpTruck = truckId,
                washing = true,
                manager = managerId,
            )

        assertEquals(id, shipment.id)
        assertEquals("Test Shipment", shipment.title)
        assertEquals(SwgType.RIVER_SAND, shipment.swg)
        assertEquals("т", shipment.measure)
        assertEquals(BigDecimal("100.50"), shipment.count)
        assertEquals(BigDecimal("500.00"), shipment.price)
        assertEquals(BigDecimal("45.00"), shipment.cost)
        assertEquals(dateTime, shipment.shipmentDateTime)
        assertEquals(truckId, shipment.dumpTruck)
        assertTrue(shipment.washing)
        assertEquals(managerId, shipment.manager)
    }

    @Test
    fun dataClassImplementsEqualsCorrectly() {
        val id = UUID.randomUUID()
        val truckId = UUID.randomUUID()
        val managerId = UUID.randomUUID()
        val dateTime = LocalDateTime.of(2024, 5, 15, 10, 30)

        val shipment1 =
            Shipment(
                id = id,
                title = "Test",
                swg = SwgType.RIVER_SAND,
                measure = "т",
                count = BigDecimal("100"),
                price = BigDecimal("500"),
                cost = BigDecimal("45"),
                shipmentDateTime = dateTime,
                dumpTruck = truckId,
                washing = true,
                manager = managerId,
            )

        val shipment2 =
            Shipment(
                id = id,
                title = "Test",
                swg = SwgType.RIVER_SAND,
                measure = "т",
                count = BigDecimal("100"),
                price = BigDecimal("500"),
                cost = BigDecimal("45"),
                shipmentDateTime = dateTime,
                dumpTruck = truckId,
                washing = true,
                manager = managerId,
            )

        assertEquals(shipment1, shipment2)
        assertEquals(shipment1.hashCode(), shipment2.hashCode())
    }

    @Test
    fun shipmentsWithDifferentIdsAreNotEqual() {
        val truckId = UUID.randomUUID()
        val managerId = UUID.randomUUID()
        val dateTime = LocalDateTime.now()

        val shipment1 = createShipment(UUID.randomUUID(), truckId, managerId, dateTime)
        val shipment2 = createShipment(UUID.randomUUID(), truckId, managerId, dateTime)

        assertNotEquals(shipment1, shipment2)
    }

    @Test
    fun copyCreatesModifiedCopy() {
        val shipment =
            createShipment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
            )

        val copied = shipment.copy(title = "Modified", washing = false)

        assertEquals("Modified", copied.title)
        assertFalse(copied.washing)
        assertEquals(shipment.id, copied.id)
        assertEquals(shipment.swg, copied.swg)
    }

    @Test
    fun supportsDifferentSwgTypes() {
        val truckId = UUID.randomUUID()
        val managerId = UUID.randomUUID()
        val dateTime = LocalDateTime.now()

        SwgType.entries.forEach { swgType ->
            val shipment =
                Shipment(
                    id = UUID.randomUUID(),
                    title = "Test ${swgType.displayName}",
                    swg = swgType,
                    measure = "т",
                    count = BigDecimal("100"),
                    price = BigDecimal("500"),
                    cost = BigDecimal("45"),
                    shipmentDateTime = dateTime,
                    dumpTruck = truckId,
                    washing = true,
                    manager = managerId,
                )
            assertEquals(swgType, shipment.swg)
        }
    }

    @Test
    fun supportsMeasureInTonnes() {
        val shipment = createShipmentWithMeasure("т")
        assertEquals("т", shipment.measure)
    }

    @Test
    fun supportsMeasureInCubicMeters() {
        val shipment = createShipmentWithMeasure("м3")
        assertEquals("м3", shipment.measure)
    }

    private fun createShipment(
        id: UUID,
        truckId: UUID,
        managerId: UUID,
        dateTime: LocalDateTime,
    ): Shipment =
        Shipment(
            id = id,
            title = "Test",
            swg = SwgType.RIVER_SAND,
            measure = "т",
            count = BigDecimal("100"),
            price = BigDecimal("500"),
            cost = BigDecimal("45"),
            shipmentDateTime = dateTime,
            dumpTruck = truckId,
            washing = true,
            manager = managerId,
        )

    private fun createShipmentWithMeasure(measure: String): Shipment =
        Shipment(
            id = UUID.randomUUID(),
            title = "Test",
            swg = SwgType.RIVER_SAND,
            measure = measure,
            count = BigDecimal("100"),
            price = BigDecimal("500"),
            cost = BigDecimal("45"),
            shipmentDateTime = LocalDateTime.now(),
            dumpTruck = UUID.randomUUID(),
            washing = true,
            manager = UUID.randomUUID(),
        )
}
