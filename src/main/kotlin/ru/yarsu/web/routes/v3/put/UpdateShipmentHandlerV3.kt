package ru.yarsu.web.routes.v3.put

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import ru.yarsu.internal.Roles
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.json.asValidRequestOrNull
import ru.yarsu.web.routes.dto.json.validateToErrors
import ru.yarsu.web.routes.filter.AuthKeys
import ru.yarsu.web.routes.lensValidate.json.rawShipmentRequestLens
import ru.yarsu.web.routes.lensValidate.path.shipmentIdLens
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal
import java.math.RoundingMode

fun updateShipmentHandlerV3(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    updateShipmentHandlerV3@{ request: Request ->

        val currentEmployeeId =
            AuthKeys.employeeIdKey(request)
                ?: return@updateShipmentHandlerV3 GetResponse.responseUnauthorized(
                    mapOf("Error" to "Отказанно в авторизации"),
                )

        val currentRole =
            AuthKeys.employeeRoleKey(request)
                ?: return@updateShipmentHandlerV3 GetResponse.responseUnauthorized(
                    mapOf("Error" to "Отказанно в авторизации"),
                )

        val shipmentId =
            try {
                shipmentIdLens(request)
            } catch (_: LensFailure) {
                return@updateShipmentHandlerV3 GetResponse.responseBadRequest(
                    mapOf("Error" to "Некорректный идентификатор акта отгрузки"),
                )
            }

        val existingShipment =
            shipmentStorage.getShipmentById(shipmentId)
                ?: return@updateShipmentHandlerV3 GetResponse.responseNotFound(
                    mapOf(
                        "ShipmentId" to shipmentId.toString(),
                        "Error" to "Акт отгрузки ПГС не найден",
                    ),
                )

        val isManager = currentRole == Roles.Manager
        val isOwner = currentEmployeeId == existingShipment.manager
        if (!isManager && !isOwner) {
            return@updateShipmentHandlerV3 GetResponse.responseUnauthorized(
                mapOf("Error" to "Отказанно в авторизации"),
            )
        }

        val rawRequest =
            try {
                rawShipmentRequestLens(request)
            } catch (_: LensFailure) {
                return@updateShipmentHandlerV3 GetResponse.responseBadRequest(
                    mapOf(
                        "Value" to request.bodyString(),
                        "Error" to "Тело запроса не является JSON-документом",
                    ),
                )
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@updateShipmentHandlerV3 GetResponse.responseBadRequest(validationErrors)
        }

        val body =
            rawRequest.asValidRequestOrNull()
                ?: return@updateShipmentHandlerV3 GetResponse.responseBadRequest(
                    mapOf("Error" to "Неизвестная ошибка при преобразовании запроса"),
                )

        val newShipmentDateTime =
            if (rawRequest.shipmentDateTime == null) existingShipment.shipmentDateTime else body.shipmentDateTime

        val newWashing =
            if (rawRequest.washing == null) existingShipment.washing else body.washing

        val requestedManagerId = body.managerId
        val newManagerId = requestedManagerId ?: existingShipment.manager

        if (requestedManagerId != null && requestedManagerId != existingShipment.manager && !isManager) {
            val targetEmployee =
                employeesStorage.getEmployeesById(requestedManagerId)
                    ?: return@updateShipmentHandlerV3 GetResponse.responseBadRequest(
                        mapOf(
                            "Manager" to
                                mapOf(
                                    "Value" to requestedManagerId.toString(),
                                    "Error" to "Работник с указанным ID не найден",
                                ),
                        ),
                    )

            return@updateShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Email" to targetEmployee.email,
                    "Error" to "Роль пользователя, выполнившего запрос, не позволяет обновить отгрузку с переданным пользователем.",
                ),
            )
        }

        val truck = trucksStorage.getTruckById(body.dumpTruckId)
        val employee = employeesStorage.getEmployeesById(newManagerId)

        val refErrors = mutableMapOf<String, Map<String, Any?>>()
        if (truck == null) {
            refErrors["DumpTruck"] =
                mapOf(
                    "Value" to body.dumpTruckId.toString(),
                    "Error" to "Самосвал с указанным ID не найден",
                )
        }
        if (employee == null) {
            refErrors["Manager"] =
                mapOf(
                    "Value" to newManagerId.toString(),
                    "Error" to "Работник с указанным ID не найден",
                )
        }
        if (refErrors.isNotEmpty()) {
            return@updateShipmentHandlerV3 GetResponse.responseBadRequest(refErrors)
        }

        val safeTruck =
            truck ?: return@updateShipmentHandlerV3 GetResponse.responseBadRequest(
                mapOf("DumpTruck" to mapOf("Value" to body.dumpTruckId.toString(), "Error" to "Самосвал с указанным ID не найден")),
            )
        val safeEmployee =
            employee ?: return@updateShipmentHandlerV3 GetResponse.responseBadRequest(
                mapOf("Manager" to mapOf("Value" to newManagerId.toString(), "Error" to "Работник с указанным ID не найден")),
            )

        if (newShipmentDateTime.isBefore(safeEmployee.registrationDateTime)) {
            return@updateShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно обновить отгрузку, так как работник не был зарегистрирован в системе (ShipmentDateTime < Employee->RegistrationDateTime)",
                ),
            )
        }

        if (body.measure == "т" && body.count > BigDecimal.valueOf(safeTruck.capacity)) {
            return@updateShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес (Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        } else if (body.measure == "м3" && body.count > BigDecimal.valueOf(safeTruck.volume)) {
            return@updateShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес (Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        }

        if (body.measure == "т") {
            val densityBd = BigDecimal.valueOf(body.swg.density)
            val volumeInCubicMeters = body.count.divide(densityBd, 6, RoundingMode.HALF_UP)
            if (volumeInCubicMeters > BigDecimal.valueOf(safeTruck.volume)) {
                return@updateShipmentHandlerV3 GetResponse.responseForbidden(
                    mapOf(
                        "Error" to
                            "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес (Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
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
                shipmentDateTime = newShipmentDateTime,
                washing = newWashing,
                dumpTruck = body.dumpTruckId,
                manager = newManagerId,
            )

        if (success) {
            GetResponse.responseNoContent()
        } else {
            GetResponse.responseNotFound(
                mapOf(
                    "ShipmentId" to shipmentId.toString(),
                    "Error" to "Акт отгрузки ПГС не найден",
                ),
            )
        }
    }
