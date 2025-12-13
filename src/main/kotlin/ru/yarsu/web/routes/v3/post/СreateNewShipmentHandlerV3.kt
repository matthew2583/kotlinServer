package ru.yarsu.web.routes.v3.post

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
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal

fun createShipmentHandlerV3(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    createShipmentHandlerV3@{ request: Request ->

        fun unauthorized(msg: String) = GetResponse.responseUnauthorized(mapOf("Error" to msg))

        val requesterId =
            AuthKeys.employeeIdKey(request)
                ?: return@createShipmentHandlerV3 unauthorized("Не удалось определить пользователя (нет employeeId).")

        val requesterRole =
            AuthKeys.employeeRoleKey(request)
                ?: return@createShipmentHandlerV3 unauthorized("Не удалось определить роль пользователя (нет employeeRole).")

        val requesterEmail =
            AuthKeys.employeeEmailKey(request)
                ?: return@createShipmentHandlerV3 unauthorized("Не удалось определить email пользователя (нет employeeEmail).")

        if (requesterRole != Roles.Employee && requesterRole != Roles.Manager) {
            return@createShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf("Error" to "Недостаточно прав для выполнения операции"),
            )
        }

        val rawRequest =
            try {
                rawShipmentRequestLens(request)
            } catch (_: LensFailure) {
                return@createShipmentHandlerV3 GetResponse.responseBadRequest(
                    mapOf(
                        "Value" to request.bodyString(),
                        "Error" to "Тело запроса не является JSON-документом",
                    ),
                )
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@createShipmentHandlerV3 GetResponse.responseBadRequest(validationErrors)
        }

        val body =
            rawRequest.asValidRequestOrNull()
                ?: return@createShipmentHandlerV3 GetResponse.responseBadRequest(
                    mapOf("Error" to "Неизвестная ошибка при преобразовании запроса"),
                )

        val managerId =
            body.managerId
                ?: return@createShipmentHandlerV3 GetResponse.responseBadRequest(
                    mapOf(
                        "Manager" to
                            mapOf(
                                "Value" to null,
                                "Error" to "Поле обязательно",
                            ),
                    ),
                )

        if (managerId != requesterId && requesterRole != Roles.Manager) {
            return@createShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Email" to
                        mapOf(
                            "Value" to requesterEmail,
                            "Error" to "Несоответствие пользователя и работника",
                        ),
                ),
            )
        }

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
            return@createShipmentHandlerV3 GetResponse.responseBadRequest(errors)
        }

        if (body.shipmentDateTime.isBefore(employee.registrationDateTime)) {
            return@createShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно создать отгрузку, так как работник не был зарегистрирован в системе " +
                        "(ShipmentDateTime < Employee->RegistrationDateTime)",
                ),
            )
        }

        if (body.measure == "т" && body.count > BigDecimal.valueOf(truck.capacity)) {
            return@createShipmentHandlerV3 GetResponse.responseForbidden(
                mapOf(
                    "Error" to
                        "Невозможно загрузить заданный объём (Measure=м3 и Count>DumpTruck->Volume) или вес " +
                        "(Measure=т и Count>DumpTruck->Capacity) в выбранный самосвал.",
                ),
            )
        } else if (body.measure == "м3" && body.count > BigDecimal.valueOf(truck.volume)) {
            return@createShipmentHandlerV3 GetResponse.responseForbidden(
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
