package ru.yarsu.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ru.yarsu.data.Employees
import ru.yarsu.data.Trucks
import ru.yarsu.domain.Roles
import ru.yarsu.domain.SwgType
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

class StorageIntegrationTest {
    private lateinit var shipmentStorage: ShipmentStorage
    private lateinit var trucksStorage: TrucksStorage
    private lateinit var employeesStorage: EmployeesStorage

    private lateinit var truck1: Trucks
    private lateinit var truck2: Trucks
    private lateinit var manager: Employees
    private lateinit var managerId: UUID

    private val testSecret = "test-secret"

    @BeforeEach
    fun setUp() {
        truck1 =
            Trucks(
                id = UUID.randomUUID(),
                model = "KAMAZ-6520",
                registration = "A123BV77",
                capacity = 20.0,
                volume = 14.0,
            )
        truck2 =
            Trucks(
                id = UUID.randomUUID(),
                model = "MAZ-5516",
                registration = "B456GD78",
                capacity = 25.0,
                volume = 18.0,
            )

        managerId = UUID.randomUUID()
        manager =
            Employees(
                id = managerId,
                name = "Ivanov Ivan",
                position = "Manager",
                registrationDateTime = LocalDateTime.now(),
                email = "ivanov@company.ru",
                role = Roles.Manager,
            )

        trucksStorage = TrucksStorage(listOf(truck1, truck2))
        employeesStorage =
            EmployeesStorage(
                initialEmployees = listOf(manager),
                secret = testSecret,
            )
        shipmentStorage = ShipmentStorage()
    }

    @Nested
    inner class ShipmentWithTruckTests {
        @Test
        fun shipmentReferencesExistingTruck() {
            val shipmentId =
                shipmentStorage.addShipment(
                    title = "Test Shipment",
                    swg = SwgType.RIVER_SAND,
                    measure = "t",
                    count = BigDecimal("100"),
                    price = BigDecimal("500"),
                    cost = BigDecimal("45"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = true,
                    dumpTruckId = truck1.id,
                    managerId = managerId,
                )

            val shipment = shipmentStorage.getShipmentById(shipmentId)
            val truck = trucksStorage.getTruckById(shipment?.dumpTruck)

            assertNotNull(truck)
            assertEquals(truck1.id, truck?.id)
        }

        @Test
        fun multipleShipmentsCanUseSameTruck() {
            repeat(3) {
                shipmentStorage.addShipment(
                    title = "Shipment $it",
                    swg = SwgType.GRANITE_RUBBLE,
                    measure = "m3",
                    count = BigDecimal("50"),
                    price = BigDecimal("1000"),
                    cost = BigDecimal("30"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = false,
                    dumpTruckId = truck2.id,
                    managerId = managerId,
                )
            }

            val shipments = shipmentStorage.getAllShipments()
            val truck2Shipments = shipments.filter { it.dumpTruck == truck2.id }

            assertEquals(3, truck2Shipments.size)
        }
    }

    @Nested
    inner class TruckCreationTests {
        @Test
        fun findOrCreateCreatesNewTruckWithShipment() {
            val swgType = SwgType.RIVER_SAND
            val weight = 30.0

            val (truck, created, _) =
                trucksStorage.findOrCreateTruck(
                    model = "New Truck",
                    registration = "N111NN99",
                    weight = weight,
                    swgType = swgType,
                )

            assertNotNull(truck)
            assertTrue(created)

            val truckId = truck?.id ?: error("Truck should not be null")

            val shipmentId =
                shipmentStorage.addShipment(
                    title = "New Truck Shipment",
                    swg = swgType,
                    measure = "t",
                    count = BigDecimal(weight.toString()),
                    price = BigDecimal("700"),
                    cost = BigDecimal("20"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = false,
                    dumpTruckId = truckId,
                    managerId = managerId,
                )

            val shipment = shipmentStorage.getShipmentById(shipmentId)
            assertEquals(truckId, shipment?.dumpTruck)
            assertNotNull(trucksStorage.getTruckById(truckId))
        }

        @Test
        fun findExistingTruckForShipment() {
            val (truck, created, _) =
                trucksStorage.findOrCreateTruck(
                    truck1.model,
                    truck1.registration,
                    36.0,
                    SwgType.RIVER_SAND,
                )

            assertNotNull(truck)
            assertFalse(created)
            assertEquals(truck1.id, truck?.id)
        }
    }

    @Nested
    inner class ShipmentFilteringTests {
        @BeforeEach
        fun setUpShipments() {
            shipmentStorage.addShipment(
                title = "January Shipment",
                swg = SwgType.RIVER_SAND,
                measure = "t",
                count = BigDecimal("100"),
                price = BigDecimal("500"),
                cost = BigDecimal("45"),
                shipmentDateTime = LocalDateTime.of(2024, 1, 15, 10, 0),
                washing = true,
                dumpTruckId = truck1.id,
                managerId = managerId,
            )
            shipmentStorage.addShipment(
                title = "February Shipment",
                swg = SwgType.GRANITE_RUBBLE,
                measure = "m3",
                count = BigDecimal("50"),
                price = BigDecimal("1000"),
                cost = BigDecimal("30"),
                shipmentDateTime = LocalDateTime.of(2024, 2, 20, 14, 30),
                washing = false,
                dumpTruckId = truck2.id,
                managerId = managerId,
            )
        }

        @Test
        fun filterShipmentsBySwgType() {
            val shipments = shipmentStorage.getShipmentsBySwgType(SwgType.RIVER_SAND)

            assertEquals(1, shipments.size)
            assertEquals("January Shipment", shipments.first().title)
        }

        @Test
        fun filterShipmentsByMonth() {
            val shipments = shipmentStorage.getAllShipments()
            val januaryShipments = shipments.filter { it.shipmentDateTime.monthValue == 1 }

            assertEquals(1, januaryShipments.size)
        }

        @Test
        fun filterShipmentsByWashing() {
            val shipments = shipmentStorage.getAllShipments()
            val washedShipments = shipments.filter { it.washing }

            assertEquals(1, washedShipments.size)
            assertEquals("January Shipment", washedShipments.first().title)
        }
    }

    @Nested
    inner class EmployeeManagerTests {
        @Test
        fun shipmentReferencesExistingManager() {
            val shipmentId =
                shipmentStorage.addShipment(
                    title = "Managed Shipment",
                    swg = SwgType.RIVER_SAND,
                    measure = "t",
                    count = BigDecimal("100"),
                    price = BigDecimal("500"),
                    cost = BigDecimal("45"),
                    shipmentDateTime = LocalDateTime.now(),
                    washing = true,
                    dumpTruckId = truck1.id,
                    managerId = managerId,
                )

            val shipment = shipmentStorage.getShipmentById(shipmentId)
            val foundManager = employeesStorage.getEmployeesById(shipment?.manager)

            assertNotNull(foundManager)
            assertEquals(manager.name, foundManager?.name)
            assertEquals(Roles.Manager, foundManager?.role)
        }
    }
}
