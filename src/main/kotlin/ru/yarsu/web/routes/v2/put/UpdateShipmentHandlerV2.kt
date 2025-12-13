package ru.yarsu.web.routes.v2.put

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.json.asValidRequestOrNull
import ru.yarsu.web.routes.dto.json.validateToErrors
import ru.yarsu.web.routes.lensValidate.json.rawShipmentRequestLens
import ru.yarsu.web.routes.lensValidate.path.shipmentIdLens
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal
import java.math.RoundingMode

fun updateShipmentHandlerV2(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    updateShipmentHandlerV2@{ request: Request ->

        val shipmentId = shipmentIdLens(request)

        val existingShipment = shipmentStorage.getShipmentById(shipmentId)
        if (existingShipment == null) {
            return@updateShipmentHandlerV2 GetResponse.responseNotFound(
                mapOf(
                    "ShipmentId" to shipmentId.toString(),
                    "Error" to "Отгрузка с указанным ID не найдена",
                ),
            )
        }

        val rawRequest =
            try {
                rawShipmentRequestLens(request)
            } catch (_: LensFailure) {
                return@updateShipmentHandlerV2 GetResponse.responseBadRequest(
                    mapOf(
                        "Value" to request.bodyString(),
                        "Error" to "Тело запроса не является JSON-документом",
                    ),
                )
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@updateShipmentHandlerV2 GetResponse.responseBadRequest(validationErrors)
        }

        val body =
            rawRequest.asValidRequestOrNull()
                ?: return@updateShipmentHandlerV2 GetResponse.responseBadRequest(
                    mapOf("Error" to "Неизвестная ошибка при преобразовании запроса"),
                )

        val managerId =
            body.managerId
                ?: return@updateShipmentHandlerV2 GetResponse.responseBadRequest(
                    mapOf(
                        "Manager" to
                            mapOf(
                                "Value" to null,
                                "Error" to "Поле обязательно",
                            ),
                    ),
                )

        val employee = employeesStorage.getEmployeesById(body.managerId)
        val truck = trucksStorage.getTruckById(body.dumpTruckId)

        if (employee == null || truck == null) {
            val errors = mutableMapOf<String, Map<String, Any?>>()
            if (truck == null) {
                errors["DumpTruck"] =
                    mapOf(
                        "Value" to body.dumpTruckId.toString(),
                        "Error" to "Самосвал с указанным ID не найден",
                    )
            }
            if (employee == null) {
                errors["Manager"] =
                    mapOf(
                        "Value" to body.managerId.toString(),
                        "Error" to "Работник с указанным ID не найден",
                    )
            }
            return@updateShipmentHandlerV2 GetResponse.responseBadRequest(errors)
        }

        if (body.shipmentDateTime.isBefore(employee.registrationDateTime)) {
            return@updateShipmentHandlerV2 GetResponse.responseForbidden(
                mapOf(
                    "Error" to "Невозможно создать отгрузку, так как работник не был зарегистрирован в системе " +
                        "(ShipmentDateTime < Employee->RegistrationDateTime)",
                ),
            )
        }

        if (body.measure == "т" && body.count > BigDecimal.valueOf(truck.capacity)) {
            return@updateShipmentHandlerV2 GetResponse.responseForbidden(
                mapOf(
                    "Error" to "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес " +
                        "(Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        } else if (body.measure == "м3" && body.count > BigDecimal.valueOf(truck.volume)) {
            return@updateShipmentHandlerV2 GetResponse.responseForbidden(
                mapOf(
                    "Error" to "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес " +
                        "(Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        }

        val densityBd = BigDecimal.valueOf(body.swg.density)

        if (body.measure == "т") {
            val volumeInCubicMeters = body.count.divide(densityBd, 6, RoundingMode.HALF_UP)
            if (volumeInCubicMeters > BigDecimal.valueOf(truck.volume)) {
                return@updateShipmentHandlerV2 GetResponse.responseForbidden(
                    mapOf(
                        "Error" to "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или " +
                            "вес (Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                    ),
                )
            }
        }

        val success =
            shipmentStorage.updateShipment(
                id = shipmentId,
                title = body.title,
                swg = body.swg,
                measure = body.measure,
                count = body.count,
                price = body.price,
                cost = body.cost,
                shipmentDateTime = body.shipmentDateTime,
                washing = body.washing,
                dumpTruck = body.dumpTruckId,
                manager = body.managerId,
            )

        if (success) {
            GetResponse.responseNoContent()
        } else {
            GetResponse.responseNotFound(
                mapOf(
                    "ShipmentId" to shipmentId.toString(),
                    "Error" to "Отгрузка с указанным ID не найдена",
                ),
            )
        }
    }
