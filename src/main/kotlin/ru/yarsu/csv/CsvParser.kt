package ru.yarsu.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import ru.yarsu.data.Employees
import ru.yarsu.data.Shipment
import ru.yarsu.data.Trucks
import ru.yarsu.domain.Roles
import ru.yarsu.domain.SwgType
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

class CsvParser {
    fun shipmentParser(path: File): List<Shipment> =
        csvReader().readAllWithHeader(path).map { row ->
            val count = validatePositiveDecimal(row.getValue("Count"), "Количество")
            val price = validatePositiveDecimal(row.getValue("Price"), "Цена")
            val cost: BigDecimal = validateCost(row.getValue("Cost").toBigDecimal(), price, count)

            Shipment(
                id = validateID(row.getValue("Id")),
                title = row.getValue("Title"),
                swg = validateSWG(row.getValue("SWG")),
                measure = validateMeasure(row.getValue("Measure")),
                count = count,
                price = price,
                cost = cost,
                shipmentDateTime = validateShipmentDateTime(row.getValue("ShipmentDateTime")),
                dumpTruck = validateID(row.getValue("DumpTruck")),
                washing = validateBoolean(row.getValue("Washing")),
                manager = validateID(row.getValue("Manager")),
            )
        }

    fun trucksParser(path: File): List<Trucks> =
        csvReader().readAllWithHeader(path).map { row ->
            Trucks(
                id = validateID(row.getValue("Id")),
                model = row.getValue("Model"),
                registration = row.getValue("Registration"),
                capacity = validateCapacity(row.getValue("Capacity")),
                volume = validateVolume(row.getValue("Volume")),
            )
        }

    fun employeesParser(path: File): List<Employees> =
        csvReader().readAllWithHeader(path).map { row ->
            Employees(
                id = validateID(row.getValue("Id")),
                name = row.getValue("Name"),
                position = row.getValue("Position"),
                registrationDateTime = validateShipmentDateTime(row.getValue("RegistrationDateTime")),
                email = row.getValue("Email"),
                role = validateRoles(row.getValue("Role")),
            )
        }

    private fun validateID(value: String): UUID =
        try {
            UUID.fromString(value)
        } catch (_: Exception) {
            throw IllegalArgumentException("Ошибка: некорректный UUID - $value")
        }

    private fun validateSWG(value: String): SwgType = SwgType.fromString(value)

    private fun validateRoles(value: String): Roles = Roles.fromString(value)

    private fun validateMeasure(value: String): String {
        val t = value
        return when (t) {
            "м3", "т" -> t
            else -> throw IllegalArgumentException("Ошибка: некорректная единица измерения (Measure) - $value")
        }
    }

    private fun validatePositiveDecimal(
        value: String,
        fieldName: String,
    ): BigDecimal =
        try {
            val bd = value.toBigDecimal()
            if (bd <= BigDecimal.ZERO) {
                throw IllegalArgumentException("Ошибка: поле $fieldName должно быть положительным - $value")
            }
            bd
        } catch (_: NumberFormatException) {
            throw IllegalArgumentException("Ошибка: поле $fieldName должно быть числом - $value")
        }

    private fun validateCost(
        cost: BigDecimal,
        price: BigDecimal,
        count: BigDecimal,
    ): BigDecimal {
        val maxCost = price.multiply(count).divide(BigDecimal(1000))
        if (cost > maxCost) {
            throw IllegalArgumentException(
                "Ошибка: Cost не может быть больше Price*Count/1000 cost=$cost, " +
                    "max=$maxCost",
            )
        }
        return cost
    }

    private fun validateShipmentDateTime(value: String): LocalDateTime =
        try {
            LocalDateTime.parse(value)
        } catch (_: DateTimeParseException) {
            throw IllegalArgumentException("Ошибка: некорректная дата/время ShipmentDateTime - $value")
        }

    private fun validateBoolean(value: String): Boolean {
        val t = value.trim().lowercase()
        return when (t) {
            "true", "false" -> t.toBoolean()
            else -> throw IllegalArgumentException("Ошибка: поле Washing должно быть true/false - $value")
        }
    }

    private fun validateCapacity(value: String): Double =
        try {
            val valueDouble = value.toDouble()
            if (valueDouble <= 0) {
                throw IllegalArgumentException("Ошибка: значение в поле capacity должно быть больше нуля")
            }
            valueDouble
        } catch (_: NumberFormatException) {
            throw IllegalArgumentException("Ошибка: в поле capacity должно быть значение Double")
        }

    private fun validateVolume(value: String): Double =
        try {
            val valueDouble = value.toDouble()
            if (valueDouble <= 0) {
                throw IllegalArgumentException("Ошибка: значение в поле volume должно быть больше нуля")
            }
            valueDouble
        } catch (_: NumberFormatException) {
            throw IllegalArgumentException("Ошибка: в поле volume должно быть значение Double")
        }
}
