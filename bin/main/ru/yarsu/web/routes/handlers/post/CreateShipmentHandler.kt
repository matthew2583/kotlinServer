package ru.yarsu.web.routes.handlers.post

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.LensFailure
import ru.yarsu.domain.Roles
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.dto.json.asValidRequestOrNull
import ru.yarsu.web.routes.dto.json.validateToErrors
import ru.yarsu.web.routes.lens.json.rawShipmentRequestLens
import ru.yarsu.web.routes.util.AuthResult
import ru.yarsu.web.routes.util.AuthUtils
import ru.yarsu.web.routes.util.ErrorMessages
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.ReferencesResult
import ru.yarsu.web.routes.util.ValidationUtils
import java.math.BigDecimal

fun createShipmentHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    createShipmentHandler@{ request: Request ->

        val auth = AuthUtils.requireRoles(request, Roles.Employee, Roles.Manager)
        val (requesterId, requesterRole, requesterEmail) =
            when (auth) {
                is AuthResult.Failure -> return@createShipmentHandler auth.response
                is AuthResult.Success -> Triple(auth.id, auth.role, auth.email)
            }

        val rawRequest =
            try {
                rawShipmentRequestLens(request)
            } catch (_: LensFailure) {
                return@createShipmentHandler GetResponse.responseBadRequest(mapOf("Error" to ErrorMessages.INVALID_JSON))
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@createShipmentHandler GetResponse.responseBadRequest(validationErrors)
        }

        val body =
            rawRequest.asValidRequestOrNull()
                ?: return@createShipmentHandler GetResponse.responseBadRequest(
                    mapOf("Error" to ErrorMessages.CONVERSION_ERROR),
                )

        val managerId =
            body.managerId
                ?: return@createShipmentHandler GetResponse.responseBadRequest(
                    mapOf(
                        "Manager" to
                            mapOf(
                                "Value" to null,
                                "Error" to "Поле обязательно",
                            ),
                    ),
                )

        if (managerId != requesterId && requesterRole != Roles.Manager) {
            return@createShipmentHandler GetResponse.responseForbidden(
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

        val refs = ValidationUtils.validateReferences(truck, body.dumpTruckId, employee, body.managerId)
        val (safeTruck, safeEmployee) =
            when (refs) {
                is ReferencesResult.Failure -> return@createShipmentHandler GetResponse.responseBadRequest(refs.errors)
                is ReferencesResult.Success -> refs.truck to refs.employee
            }

        if (body.shipmentDateTime.isBefore(safeEmployee.registrationDateTime)) {
            return@createShipmentHandler GetResponse.responseForbidden(
                mapOf("Error" to ErrorMessages.EMPLOYEE_NOT_REGISTERED_CREATE),
            )
        }

        val capacityError = ValidationUtils.validateTruckCapacity(body.measure, body.count, safeTruck)
        if (capacityError != null) return@createShipmentHandler capacityError

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
