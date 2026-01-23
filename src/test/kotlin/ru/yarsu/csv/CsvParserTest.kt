package ru.yarsu.csv

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import ru.yarsu.domain.Roles
import ru.yarsu.domain.SwgType
import java.math.BigDecimal
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

class CsvParserTest {
    private lateinit var csvParser: CsvParser

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        csvParser = CsvParser()
    }

    @Test
    fun parseValidShipmentCsv() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Тестовая отгрузка,Песок речной,т,36,640,22.3488,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val result = csvParser.shipmentParser(file)

        assertEquals(1, result.size)
        val shipment = result.first()
        assertEquals(UUID.fromString("de42a00f-7f43-4d10-808d-bee47fdeef49"), shipment.id)
        assertEquals("Тестовая отгрузка", shipment.title)
        assertEquals(SwgType.RIVER_SAND, shipment.swg)
        assertEquals("т", shipment.measure)
        assertEquals(BigDecimal("36"), shipment.count)
        assertEquals(BigDecimal("640"), shipment.price)
        assertEquals(BigDecimal("22.3488"), shipment.cost)
        assertTrue(shipment.washing)
    }

    @Test
    fun parseMultipleShipments() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Отгрузка 1,Песок речной,т,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            2b52fcb1-34ec-48d2-95f2-b79f2d9c3c70,Отгрузка 2,Щебень гранитный,м3,14,2660,36,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,False,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val result = csvParser.shipmentParser(file)

        assertEquals(2, result.size)
    }

    @Test
    fun invalidUuidThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            invalid-uuid,Test,Песок речной,т,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("UUID"))
    }

    @Test
    fun unknownSwgTypeThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Неизвестный тип,т,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("Неизвестный тип ПГС"))
    }

    @Test
    fun invalidMeasureThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,кг,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("Measure"))
    }

    @Test
    fun measureTonnesIsAccepted() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val result = csvParser.shipmentParser(file)
        assertEquals("т", result[0].measure)
    }

    @Test
    fun measureCubicMetersIsAccepted() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,м3,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val result = csvParser.shipmentParser(file)
        assertEquals("м3", result[0].measure)
    }

    @Test
    fun negativeCountThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,-36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("положительным"))
    }

    @Test
    fun zeroPriceThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,36,0,0,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("положительным"))
    }

    @Test
    fun costExceedsMaxThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,10,100,10,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("Cost"))
    }

    @Test
    fun invalidDateFormatThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,36,640,22,01-01-2024,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("дата") || message.contains("ShipmentDateTime"))
    }

    @Test
    fun invalidWashingValueThrowsException() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,yes,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.shipmentParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("Washing"))
    }

    @Test
    fun washingFalseIsParsedCorrectly() {
        val csv =
            """
            Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Песок речной,т,36,640,22,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,false,de42a00f-7f43-4d10-808d-bee47fdeef49
            """.trimIndent()
        val file = tempDir.resolve("shipments.csv").toFile()
        file.writeText(csv)

        val result = csvParser.shipmentParser(file)
        assertFalse(result[0].washing)
    }

    @Test
    fun parseValidTrucksCsv() {
        val csv =
            """
            Id,Model,Registration,Capacity,Volume
            accfe76f-9a9e-4cb4-8876-d36daa22f924,KAMAZ-65951-СА,Д903ЧН,36.0,25
            """.trimIndent()
        val file = tempDir.resolve("trucks.csv").toFile()
        file.writeText(csv)

        val result = csvParser.trucksParser(file)

        assertEquals(1, result.size)
        val truck = result.first()
        assertEquals(UUID.fromString("accfe76f-9a9e-4cb4-8876-d36daa22f924"), truck.id)
        assertEquals("KAMAZ-65951-СА", truck.model)
        assertEquals("Д903ЧН", truck.registration)
        assertEquals(36.0, truck.capacity)
        assertEquals(25.0, truck.volume)
    }

    @Test
    fun parseMultipleTrucks() {
        val csv =
            """
            Id,Model,Registration,Capacity,Volume
            accfe76f-9a9e-4cb4-8876-d36daa22f924,KAMAZ-65951-СА,Д903ЧН,36.0,25
            f8741785-4986-41ab-85d3-8b2fe522f9ed,МАЗ 5516А8-336,Э723ВЛ,37.0,26
            """.trimIndent()
        val file = tempDir.resolve("trucks.csv").toFile()
        file.writeText(csv)

        val result = csvParser.trucksParser(file)
        assertEquals(2, result.size)
    }

    @Test
    fun nonNumericCapacityThrowsException() {
        val csv =
            """
            Id,Model,Registration,Capacity,Volume
            accfe76f-9a9e-4cb4-8876-d36daa22f924,KAMAZ,Д903ЧН,abc,25
            """.trimIndent()
        val file = tempDir.resolve("trucks.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.trucksParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("capacity"))
    }

    @Test
    fun zeroCapacityThrowsException() {
        val csv =
            """
            Id,Model,Registration,Capacity,Volume
            accfe76f-9a9e-4cb4-8876-d36daa22f924,KAMAZ,Д903ЧН,0,25
            """.trimIndent()
        val file = tempDir.resolve("trucks.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.trucksParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("capacity"))
    }

    @Test
    fun negativeVolumeThrowsException() {
        val csv =
            """
            Id,Model,Registration,Capacity,Volume
            accfe76f-9a9e-4cb4-8876-d36daa22f924,KAMAZ,Д903ЧН,36,-5
            """.trimIndent()
        val file = tempDir.resolve("trucks.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.trucksParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("volume"))
    }

    @Test
    fun parseValidEmployeesCsv() {
        val csv =
            """
            Id,Name,Position,RegistrationDateTime,Email,Role
            de42a00f-7f43-4d10-808d-bee47fdeef49,Иванов Иван Иванович,Начальник смены,2024-01-01T00:00:00,ivanov@example.com,Manager
            """.trimIndent()
        val file = tempDir.resolve("employees.csv").toFile()
        file.writeText(csv)

        val result = csvParser.employeesParser(file)

        assertEquals(1, result.size)
        val employee = result.first()
        assertEquals(UUID.fromString("de42a00f-7f43-4d10-808d-bee47fdeef49"), employee.id)
        assertEquals("Иванов Иван Иванович", employee.name)
        assertEquals("Начальник смены", employee.position)
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), employee.registrationDateTime)
        assertEquals("ivanov@example.com", employee.email)
        assertEquals(Roles.Manager, employee.role)
    }

    @Test
    fun employeeRoleIsParsedCorrectly() {
        val csv =
            """
            Id,Name,Position,RegistrationDateTime,Email,Role
            de42a00f-7f43-4d10-808d-bee47fdeef49,Петров Пётр,Работник,2024-01-01T00:00:00,petrov@example.com,Employee
            """.trimIndent()
        val file = tempDir.resolve("employees.csv").toFile()
        file.writeText(csv)

        val result = csvParser.employeesParser(file)
        assertEquals(Roles.Employee, result.first().role)
    }

    @Test
    fun userManagerRoleIsParsedCorrectly() {
        val csv =
            """
            Id,Name,Position,RegistrationDateTime,Email,Role
            de42a00f-7f43-4d10-808d-bee47fdeef49,Сидоров Сидор,Администратор,2024-01-01T00:00:00,sidorov@example.com,UserManager
            """.trimIndent()
        val file = tempDir.resolve("employees.csv").toFile()
        file.writeText(csv)

        val result = csvParser.employeesParser(file)
        assertEquals(Roles.UserManager, result.first().role)
    }

    @Test
    fun invalidRoleThrowsException() {
        val csv =
            """
            Id,Name,Position,RegistrationDateTime,Email,Role
            de42a00f-7f43-4d10-808d-bee47fdeef49,Test,Position,2024-01-01T00:00:00,test@example.com,Admin
            """.trimIndent()
        val file = tempDir.resolve("employees.csv").toFile()
        file.writeText(csv)

        val exception =
            assertThrows<IllegalArgumentException> {
                csvParser.employeesParser(file)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("роль"))
    }

    @Test
    fun parseMultipleEmployees() {
        val csv =
            """
            Id,Name,Position,RegistrationDateTime,Email,Role
            de42a00f-7f43-4d10-808d-bee47fdeef49,Иванов,Менеджер,2024-01-01T00:00:00,ivanov@example.com,Manager
            2b52fcb1-34ec-48d2-95f2-b79f2d9c3c70,Петров,Работник,2024-02-15T10:30:00,petrov@example.com,Employee
            """.trimIndent()
        val file = tempDir.resolve("employees.csv").toFile()
        file.writeText(csv)

        val result = csvParser.employeesParser(file)
        assertEquals(2, result.size)
    }

    @Test
    fun emptyCsvReturnsEmptyList() {
        val csv = "Id,Model,Registration,Capacity,Volume"
        val file = tempDir.resolve("empty.csv").toFile()
        file.writeText(csv)

        val result = csvParser.trucksParser(file)
        assertTrue(result.isEmpty())
    }
}
