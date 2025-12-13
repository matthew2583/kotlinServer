package ru.yarsu.web.routes.v3.post

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import ru.yarsu.internal.Roles
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.form.asValidFormOrNull
import ru.yarsu.web.routes.dto.form.validateToErrors
import ru.yarsu.web.routes.filter.AuthKeys
import ru.yarsu.web.routes.lensValidate.form.rawInvoiceFormLens
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal
import java.time.LocalDateTime

fun invoiceRegistrationHandlerV3(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    invoiceRegistrationHandlerV3@{ request: Request ->

        val requesterId =
            AuthKeys.employeeIdKey(request)
                ?: return@invoiceRegistrationHandlerV3 GetResponse.responseUnauthorized(
                    mapOf("Error" to "Отказанно в авторизации"),
                )

        val requesterRole =
            AuthKeys.employeeRoleKey(request)
                ?: return@invoiceRegistrationHandlerV3 GetResponse.responseUnauthorized(
                    mapOf("Error" to "Отказанно в авторизации"),
                )

        if (requesterRole != Roles.Manager) {
            return@invoiceRegistrationHandlerV3 GetResponse.responseUnauthorized(
                mapOf("Error" to "Отказанно в авторизации"),
            )
        }

        val rawRequest =
            try {
                rawInvoiceFormLens(request)
            } catch (_: LensFailure) {
                return@invoiceRegistrationHandlerV3 GetResponse.responseBadRequest(
                    mapOf("Error" to "Тело запроса не является form-urlencoded"),
                )
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@invoiceRegistrationHandlerV3 GetResponse.responseBadRequest(validationErrors)
        }

        val validRequest =
            rawRequest.asValidFormOrNull()
                ?: return@invoiceRegistrationHandlerV3 GetResponse.responseBadRequest(
                    mapOf("Error" to "Неизвестная ошибка при преобразовании формы"),
                )

        val currentDateTime = LocalDateTime.now()

        val (truck, _, duplicates) =
            trucksStorage.findOrCreateTruck(
                model = validRequest.dumpTruckModel,
                registration = validRequest.dumpTruckRegistration,
                weight = validRequest.invoiceWeight,
                swgType = validRequest.invoiceType,
            )

        if (truck == null && duplicates.isNotEmpty()) {
            val trucksInfo =
                duplicates.map { t ->
                    mapOf(
                        "Id" to t.id.toString(),
                        "Capacity" to t.capacity,
                        "Volume" to t.volume,
                        "ShipmentsCount" to shipmentStorage.getShipmentsByTruckId(t.id).size,
                    )
                }

            return@invoiceRegistrationHandlerV3 GetResponse.responseConflict(trucksInfo)
        }

        val safeTruck =
            truck
                ?: return@invoiceRegistrationHandlerV3 GetResponse.responseBadRequest(
                    mapOf("Error" to "Ошибка при обработке самосвала"),
                )

        if (validRequest.invoiceWeight > safeTruck.capacity) {
            return@invoiceRegistrationHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Error" to "Невозможно загрузить заданный вес в выбранный самосвал " +
                        "(InvoiceWeight>DumpTruck->Capacity).",
                ),
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
                dumpTruckId = safeTruck.id,
                managerId = requesterId,
            )

        GetResponse.responseCreated(
            mapOf(
                "ShipmentId" to shipmentId.toString(),
                "DumpTruckId" to safeTruck.id.toString(),
            ),
        )
    }
