package ru.yarsu.web.routes.v2.post

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.form.asValidFormOrNull
import ru.yarsu.web.routes.dto.form.validateToErrors
import ru.yarsu.web.routes.lensValidate.form.rawInvoiceFormLens
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal
import java.time.LocalDateTime

fun invoiceRegistrationHandlerV2(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    invoiceRegistrationHandlerV2@{ request: Request ->

        val rawRequest = rawInvoiceFormLens(request)

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@invoiceRegistrationHandlerV2 GetResponse.responseBadRequest(validationErrors)
        }

        val validRequest =
            rawRequest.asValidFormOrNull()
                ?: return@invoiceRegistrationHandlerV2 GetResponse.responseBadRequest(
                    mapOf("Error" to "Неизвестная ошибка при преобразовании формы"),
                )

        val wasCostCalculated = rawRequest.invoiceCost.isNullOrBlank()

        val manager = employeesStorage.getEmployeesById(validRequest.manager)
        if (manager == null) {
            return@invoiceRegistrationHandlerV2 GetResponse.responseBadRequest(
                mapOf(
                    "Manager" to
                        mapOf(
                            "Value" to validRequest.manager.toString(),
                            "Error" to "Работник с указанным ID не найден",
                        ),
                ),
            )
        }

        val currentDateTime = LocalDateTime.now()

        val (truck, _, duplicates) =
            trucksStorage.findOrCreateTruck(
                model = validRequest.dumpTruckModel,
                registration = validRequest.dumpTruckRegistration,
                weight = validRequest.invoiceWeight,
                swgType = validRequest.invoiceType,
            )

        when {
            truck == null && duplicates.isNotEmpty() -> {
                val trucksInfo =
                    duplicates.map { t ->
                        mapOf(
                            "Id" to t.id.toString(),
                            "Capacity" to t.capacity,
                            "Volume" to t.volume,
                            "ShipmentsCount" to shipmentStorage.getShipmentsByTruckId(t.id).size,
                        )
                    }
                GetResponse.responseConflict(trucksInfo)
            }

            truck != null -> {
                if (validRequest.invoiceWeight > truck.capacity) {
                    return@invoiceRegistrationHandlerV2 GetResponse.responseForbidden(
                        mapOf("Error" to "Невозможно загрузить заданный вес в выбранный самосвал."),
                    )
                }

                val shipmentId =
                    shipmentStorage.addShipment(
                        title = validRequest.invoiceTitle,
                        swg = validRequest.invoiceType,
                        measure = "т",
                        count = BigDecimal.valueOf(validRequest.invoiceWeight),
                        price = BigDecimal.valueOf(validRequest.invoicePrice),
                        cost = BigDecimal.valueOf(validRequest.invoiceCost),
                        shipmentDateTime = currentDateTime,
                        washing = false,
                        dumpTruckId = truck.id,
                        managerId = validRequest.manager,
                    )

                val responseData =
                    mutableMapOf(
                        "ShipmentId" to shipmentId.toString(),
                        "DumpTruckId" to truck.id.toString(),
                    )

                if (wasCostCalculated) {
                    responseData["CostCalculated"] = "true"
                    responseData["CalculatedCost"] = validRequest.invoiceCost.toString()
                }

                GetResponse.responseCreated(responseData)
            }

            else -> {
                GetResponse.responseBadRequest(
                    mapOf("Error" to "Ошибка при обработке самосвала"),
                )
            }
        }
    }
