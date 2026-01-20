package ru.yarsu.web.routes.handlers.post

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import ru.yarsu.domain.Roles
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.form.asValidFormOrNull
import ru.yarsu.web.routes.dto.form.validateToErrors
import ru.yarsu.web.routes.lens.form.rawInvoiceFormLens
import ru.yarsu.web.routes.util.AuthResult
import ru.yarsu.web.routes.util.AuthUtils
import ru.yarsu.web.routes.util.ErrorMessages
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal
import java.time.LocalDateTime

fun invoiceRegistrationHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    invoiceRegistrationHandler@{ request: Request ->

        val auth = AuthUtils.requireRoles(request, Roles.Manager)
        val requesterId =
            when (auth) {
                is AuthResult.Failure -> return@invoiceRegistrationHandler auth.response
                is AuthResult.Success -> auth.id
            }

        val rawRequest =
            try {
                rawInvoiceFormLens(request)
            } catch (_: LensFailure) {
                return@invoiceRegistrationHandler GetResponse.responseBadRequest(mapOf("Error" to ErrorMessages.INVALID_FORM))
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@invoiceRegistrationHandler GetResponse.responseBadRequest(validationErrors)
        }

        val validRequest =
            rawRequest.asValidFormOrNull()
                ?: return@invoiceRegistrationHandler GetResponse.responseBadRequest(
                    mapOf("Error" to ErrorMessages.FORM_CONVERSION_ERROR),
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

            return@invoiceRegistrationHandler GetResponse.responseConflict(trucksInfo)
        }

        val safeTruck =
            truck
                ?: return@invoiceRegistrationHandler GetResponse.responseBadRequest(
                    mapOf("Error" to "Ошибка при обработке самосвала"),
                )

        if (validRequest.invoiceWeight > safeTruck.capacity) {
            return@invoiceRegistrationHandler GetResponse.responseForbidden(
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
