package ru.yarsu.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ru.yarsu.data.Shipment
import ru.yarsu.domain.SwgType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class ShipmentStorageTest {
    private lateinit var storage: ShipmentStorage

    @BeforeEach
    fun setUp() {
        storage = ShipmentStorage()
    }

    @Nested
    inner class AddShipmentTests {
        @Test
        fun addShipmentReturnsNewId() {
            val id =
                storage.addShipment(
                    title = "Test Shipment",
                    swg = SwgType.RIVER_SAND,
                    measure = "т",
                    count = BigDecimal("100"),
                    price = BigDecimal("500"),
                    cost = BigDecimal("45"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = true,
                    dumpTruckId = UUID.randomUUID(),
                    managerId = UUID.randomUUID(),
                )

            assertNotNull(id)
        }

        @Test
        fun addedShipmentCanBeRetrieved() {
            val id =
                storage.addShipment(
                    title = "Test Shipment",
                    swg = SwgType.GRANITE_RUBBLE,
                    measure = "м3",
                    count = BigDecimal("50"),
                    price = BigDecimal("1000"),
                    cost = BigDecimal("30"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = false,
                    dumpTruckId = UUID.randomUUID(),
                    managerId = UUID.randomUUID(),
                )

            val shipment = storage.getShipmentById(id)
            assertNotNull(shipment)
            assertEquals("Test Shipment", shipment?.title)
            assertEquals(SwgType.GRANITE_RUBBLE, shipment?.swg)
        }
    }

    @Nested
    inner class GetShipmentTests {
        @Test
        fun getShipmentByIdReturnsNullForNonExistent() {
            val shipment = storage.getShipmentById(UUID.randomUUID())
            assertNull(shipment)
        }

        @Test
        fun getAllShipmentsReturnsEmptyListInitially() {
            val shipments = storage.getAllShipments()
            assertTrue(shipments.isEmpty())
        }

        @Test
        fun getAllShipmentsReturnsAllAdded() {
            repeat(3) { i ->
                storage.addShipment(
                    title = "Shipment $i",
                    swg = SwgType.RIVER_SAND,
                    measure = "т",
                    count = BigDecimal("100"),
                    price = BigDecimal("500"),
                    cost = BigDecimal("45"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = true,
                    dumpTruckId = UUID.randomUUID(),
                    managerId = UUID.randomUUID(),
                )
            }

            val shipments = storage.getAllShipments()
            assertEquals(3, shipments.size)
        }
    }

    @Nested
    inner class InitWithListTests {
        @Test
        fun storageCanBeInitializedWithList() {
            val shipment =
                Shipment(
                    id = UUID.randomUUID(),
                    title = "Existing",
                    swg = SwgType.RIVER_SAND,
                    measure = "т",
                    count = BigDecimal("100"),
                    price = BigDecimal("500"),
                    cost = BigDecimal("45"),
                    shipmentDateTime = LocalDateTime.now(),
                    dumpTruck = UUID.randomUUID(),
                    washing = true,
                    manager = UUID.randomUUID(),
                )

            val storageWithData = ShipmentStorage(listOf(shipment))

            assertEquals(1, storageWithData.getAllShipments().size)
            assertNotNull(storageWithData.getShipmentById(shipment.id))
        }
    }
}
