package ru.yarsu.csv

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.yarsu.data.Shipment
import ru.yarsu.data.Trucks
import ru.yarsu.domain.SwgType
import java.io.File
import java.math.BigDecimal
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

class CsvWriteTest {
    private lateinit var csvWrite: CsvWrite

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        csvWrite = CsvWrite()
    }

    @Test
    fun writeShipmentsToCsvFile() {
        val shipment =
            Shipment(
                id = UUID.fromString("de42a00f-7f43-4d10-808d-bee47fdeef49"),
                title = "Тестовая отгрузка",
                swg = SwgType.RIVER_SAND,
                measure = "т",
                count = BigDecimal("100"),
                price = BigDecimal("500"),
                cost = BigDecimal("45"),
                shipmentDateTime = LocalDateTime.of(2024, 5, 15, 10, 30),
                dumpTruck = UUID.fromString("accfe76f-9a9e-4cb4-8876-d36daa22f924"),
                washing = true,
                manager = UUID.fromString("de42a00f-7f43-4d10-808d-bee47fdeef49"),
            )
        val filePath = tempDir.resolve("shipments.csv").toString()

        csvWrite.writeShipments(listOf(shipment), filePath)

        val file = File(filePath)
        assertTrue(file.exists())

        val content = file.readText()
        assertTrue(content.contains("Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,Washing,DumpTruck,Manager"))
        assertTrue(content.contains("de42a00f-7f43-4d10-808d-bee47fdeef49"))
        assertTrue(content.contains("Тестовая отгрузка"))
        assertTrue(content.contains("Песок речной"))
        assertTrue(content.contains("100"))
        assertTrue(content.contains("500"))
        assertTrue(content.contains("45"))
        assertTrue(content.contains("true"))
    }

    @Test
    fun writeMultipleShipments() {
        val shipment1 = createTestShipment("Отгрузка 1", SwgType.RIVER_SAND, true)
        val shipment2 = createTestShipment("Отгрузка 2", SwgType.GRANITE_RUBBLE, false)
        val filePath = tempDir.resolve("multi-shipments.csv").toString()

        csvWrite.writeShipments(listOf(shipment1, shipment2), filePath)

        val file = File(filePath)
        val lines = file.readLines()
        assertEquals(3, lines.size)
    }

    @Test
    fun writeEmptyShipmentListCreatesHeaderOnlyFile() {
        val filePath = tempDir.resolve("empty-shipments.csv").toString()

        csvWrite.writeShipments(emptyList(), filePath)

        val file = File(filePath)
        assertTrue(file.exists())
        val lines = file.readLines()
        assertEquals(1, lines.size)
        assertTrue(lines[0].contains("Id"))
    }

    @Test
    fun swgTypeIsWrittenAsDisplayName() {
        val shipment = createTestShipment("Test", SwgType.GRANITE_RUBBLE, false)
        val filePath = tempDir.resolve("swg-test.csv").toString()

        csvWrite.writeShipments(listOf(shipment), filePath)

        val content = File(filePath).readText()
        assertTrue(content.contains("Щебень гранитный"))
        assertFalse(content.contains("GRANITE_RUBBLE"))
    }

    @Test
    fun writeTrucksToCsvFile() {
        val truck =
            Trucks(
                id = UUID.fromString("accfe76f-9a9e-4cb4-8876-d36daa22f924"),
                model = "KAMAZ-65951",
                registration = "А123БВ77",
                capacity = 36.0,
                volume = 25.0,
            )
        val filePath = tempDir.resolve("trucks.csv").toString()

        csvWrite.writeTrucks(listOf(truck), filePath)

        val file = File(filePath)
        assertTrue(file.exists())

        val content = file.readText()
        assertTrue(content.contains("Id,Model,Registration,Capacity,Volume"))
        assertTrue(content.contains("accfe76f-9a9e-4cb4-8876-d36daa22f924"))
        assertTrue(content.contains("KAMAZ-65951"))
        assertTrue(content.contains("А123БВ77"))
        assertTrue(content.contains("36.0"))
        assertTrue(content.contains("25.0"))
    }

    @Test
    fun writeMultipleTrucks() {
        val truck1 = createTestTruck("KAMAZ", "А123БВ77")
        val truck2 = createTestTruck("МАЗ", "В456ГД78")
        val filePath = tempDir.resolve("multi-trucks.csv").toString()

        csvWrite.writeTrucks(listOf(truck1, truck2), filePath)

        val file = File(filePath)
        val lines = file.readLines()
        assertEquals(3, lines.size)
    }

    @Test
    fun writeEmptyTruckListCreatesHeaderOnlyFile() {
        val filePath = tempDir.resolve("empty-trucks.csv").toString()

        csvWrite.writeTrucks(emptyList(), filePath)

        val file = File(filePath)
        assertTrue(file.exists())
        val lines = file.readLines()
        assertEquals(1, lines.size)
        assertTrue(lines[0].contains("Id"))
        assertTrue(lines[0].contains("Model"))
    }

    @Test
    fun trucksWithDecimalValuesAreWrittenCorrectly() {
        val truck =
            Trucks(
                id = UUID.randomUUID(),
                model = "Test",
                registration = "TEST",
                capacity = 25.75,
                volume = 18.333,
            )
        val filePath = tempDir.resolve("decimal-trucks.csv").toString()

        csvWrite.writeTrucks(listOf(truck), filePath)

        val content = File(filePath).readText()
        assertTrue(content.contains("25.75"))
        assertTrue(content.contains("18.333"))
    }

    private fun createTestShipment(
        title: String,
        swg: SwgType,
        washing: Boolean,
    ): Shipment =
        Shipment(
            id = UUID.randomUUID(),
            title = title,
            swg = swg,
            measure = "т",
            count = BigDecimal("100"),
            price = BigDecimal("500"),
            cost = BigDecimal("45"),
            shipmentDateTime = LocalDateTime.now(),
            dumpTruck = UUID.randomUUID(),
            washing = washing,
            manager = UUID.randomUUID(),
        )

    private fun createTestTruck(
        model: String,
        registration: String,
    ): Trucks =
        Trucks(
            id = UUID.randomUUID(),
            model = model,
            registration = registration,
            capacity = 36.0,
            volume = 25.0,
        )
}
