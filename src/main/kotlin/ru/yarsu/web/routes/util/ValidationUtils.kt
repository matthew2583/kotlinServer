package ru.yarsu.web.routes.util

import org.http4k.core.Response
import ru.yarsu.data.Employees
import ru.yarsu.data.Trucks
import java.math.BigDecimal

sealed class ReferencesResult {
    data class Success(
        val truck: Trucks,
        val employee: Employees,
    ) : ReferencesResult()

    data class Failure(
        val errors: Map<String, Map<String, Any?>>,
    ) : ReferencesResult()
}

object ValidationUtils {
    private const val ERROR_CAPACITY_VOLUME =
        "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) " +
            "или вес (Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал."

    fun validateTruckCapacity(
        measure: String,
        count: BigDecimal,
        truck: Trucks,
    ): Response? {
        val isOverCapacity = measure == "т" && count > BigDecimal.valueOf(truck.capacity)
        val isOverVolume = measure == "м3" && count > BigDecimal.valueOf(truck.volume)

        return if (isOverCapacity || isOverVolume) {
            GetResponse.responseForbidden(mapOf("Error" to ERROR_CAPACITY_VOLUME))
        } else {
            null
        }
    }

    fun validateReferences(
        truck: Trucks?,
        truckId: Any,
        employee: Employees?,
        employeeId: Any,
    ): ReferencesResult {
        val errors = mutableMapOf<String, Map<String, Any?>>()

        if (truck == null) {
            errors["DumpTruck"] =
                mapOf(
                    "Value" to truckId.toString(),
                    "Error" to "Самосвал с указанным ID не найден",
                )
        }

        if (employee == null) {
            errors["Manager"] =
                mapOf(
                    "Value" to employeeId.toString(),
                    "Error" to "Работник с указанным ID не найден",
                )
        }

        return if (errors.isNotEmpty() || truck == null || employee == null) {
            ReferencesResult.Failure(errors)
        } else {
            ReferencesResult.Success(truck, employee)
        }
    }
}
