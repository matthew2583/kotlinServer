package ru.yarsu.web.routes.v2.post

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.json.asValidRequestOrNull
import ru.yarsu.web.routes.dto.json.validateToErrors
import ru.yarsu.web.routes.lensValidate.json.rawShipmentRequestLens
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal

fun createShipmentHandlerV2(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    createShipmentHandlerV2@{ request: Request ->

        val rawRequest =
            try {
                rawShipmentRequestLens(request)
            } catch (_: LensFailure) {
                return@createShipmentHandlerV2 GetResponse.responseBadRequest(
                    mapOf(
                        "Value" to request.bodyString(),
                        "Error" to "Тело запроса не является JSON-документом",
                    ),
                )
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@createShipmentHandlerV2 GetResponse.responseBadRequest(validationErrors)
        }

        val body =
            rawRequest.asValidRequestOrNull()
                ?: return@createShipmentHandlerV2 GetResponse.responseBadRequest(
                    mapOf("Error" to "Неизвестная ошибка при преобразовании запроса"),
                )

        val managerId =
            body.managerId
                ?: return@createShipmentHandlerV2 GetResponse.responseBadRequest(
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
            return@createShipmentHandlerV2 GetResponse.responseBadRequest(errors)
        }

        if (body.shipmentDateTime.isBefore(employee.registrationDateTime)) {
            return@createShipmentHandlerV2 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно создать отгрузку, так как работник не был зарегистрирован в системе " +
                        "(ShipmentDateTime < Employee->RegistrationDateTime)",
                ),
            )
        }

        if (body.measure == "т" && body.count > BigDecimal.valueOf(truck.capacity)) {
            return@createShipmentHandlerV2 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес " +
                        "(Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        } else if (body.measure == "м3" && body.count > BigDecimal.valueOf(truck.volume)) {
            return@createShipmentHandlerV2 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес " +
                        "(Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        }

        val shipmentId =
            shipmentStorage.addShipment(
                title = body.title,
                swg = body.swg,
                measure = body.measure,
                count = body.count,
                price = body.price,
                cost = body.cost,
                shipmentDateTime = body.shipmentDateTime,
                washing = body.washing,
                dumpTruckId = body.dumpTruckId,
                managerId = body.managerId,
            )

        GetResponse.responseCreated(mapOf("Id" to shipmentId.toString()))
    }
