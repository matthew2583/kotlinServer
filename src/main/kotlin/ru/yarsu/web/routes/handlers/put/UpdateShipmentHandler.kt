package ru.yarsu.web.routes.handlers.put

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
import ru.yarsu.web.routes.lens.path.shipmentIdLens
import ru.yarsu.web.routes.util.AuthResult
import ru.yarsu.web.routes.util.AuthUtils
import ru.yarsu.web.routes.util.ErrorMessages
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.ReferencesResult
import ru.yarsu.web.routes.util.ValidationUtils
import java.math.BigDecimal
import java.math.RoundingMode

fun updateShipmentHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    updateShipmentHandler@{ request: Request ->

        val auth = AuthUtils.requireRoles(request)
        val (currentEmployeeId, currentRole) =
            when (auth) {
                is AuthResult.Failure -> return@updateShipmentHandler auth.response
                is AuthResult.Success -> auth.id to auth.role
            }

        val shipmentId =
            try {
                shipmentIdLens(request)
            } catch (_: LensFailure) {
                return@updateShipmentHandler GetResponse.responseBadRequest(mapOf("Error" to ErrorMessages.INVALID_SHIPMENT_ID))
            }

        val existingShipment =
            shipmentStorage.getShipmentById(shipmentId)
                ?: return@updateShipmentHandler GetResponse.responseNotFound(
                    mapOf(
                        "ShipmentId" to shipmentId.toString(),
                        "Error" to ErrorMessages.SHIPMENT_NOT_FOUND,
                    ),
                )

        val isManager = currentRole == Roles.Manager
        val isOwner = currentEmployeeId == existingShipment.manager
        if (!isManager && !isOwner) {
            return@updateShipmentHandler GetResponse.responseUnauthorized(
                mapOf("Error" to ErrorMessages.UNAUTHORIZED),
            )
        }

        val rawRequest =
            try {
                rawShipmentRequestLens(request)
            } catch (_: LensFailure) {
                return@updateShipmentHandler GetResponse.responseBadRequest(mapOf("Error" to ErrorMessages.INVALID_JSON))
            }

        val validationErrors = rawRequest.validateToErrors()
        if (validationErrors.isNotEmpty()) {
            return@updateShipmentHandler GetResponse.responseBadRequest(validationErrors)
        }

        val body: ru.yarsu.web.routes.dto.json.ValidShipmentRequest =
            rawRequest.asValidRequestOrNull()
                ?: return@updateShipmentHandler GetResponse.responseBadRequest(
                    mapOf("Error" to ErrorMessages.CONVERSION_ERROR),
                )

        val newShipmentDateTime =
            if (rawRequest.shipmentDateTime == null) {
                existingShipment.shipmentDateTime
            } else {
                body.shipmentDateTime
            }

        val newWashing =
            if (rawRequest.washing == null) {
                existingShipment.washing
            } else {
                body.washing
            }

        val requestedManagerId = body.managerId
        val newManagerId = requestedManagerId ?: existingShipment.manager

        val truck = trucksStorage.getTruckById(body.dumpTruckId)
        val employee = employeesStorage.getEmployeesById(newManagerId)

        if (requestedManagerId != null && requestedManagerId != existingShipment.manager && !isManager) {
            if (employee == null) {
                return@updateShipmentHandler GetResponse.responseBadRequest(
                    mapOf(
                        "Manager" to
                            mapOf(
                                "Value" to requestedManagerId.toString(),
                                "Error" to ErrorMessages.EMPLOYEE_NOT_FOUND,
                            ),
                    ),
                )
            }
            return@updateShipmentHandler GetResponse.responseForbidden(
                mapOf(
                    "Email" to employee.email,
                    "Error" to ErrorMessages.CANNOT_CHANGE_MANAGER,
                ),
            )
        }

        val refs = ValidationUtils.validateReferences(truck, body.dumpTruckId, employee, newManagerId)
        val (safeTruck, safeEmployee) =
            when (refs) {
                is ReferencesResult.Failure -> return@updateShipmentHandler GetResponse.responseBadRequest(refs.errors)
                is ReferencesResult.Success -> refs.truck to refs.employee
            }

        if (newShipmentDateTime.isBefore(safeEmployee.registrationDateTime)) {
            return@updateShipmentHandler GetResponse.responseForbidden(
                mapOf("Error" to ErrorMessages.EMPLOYEE_NOT_REGISTERED),
            )
        }

        val capacityError = ValidationUtils.validateTruckCapacity(body.measure, body.count, safeTruck)
        if (capacityError != null) return@updateShipmentHandler capacityError

        if (body.measure == "т") {
            val densityBd = BigDecimal.valueOf(body.swg.density)
            val volumeInCubicMeters = body.count.divide(densityBd, 6, RoundingMode.HALF_UP)
            val volumeError = ValidationUtils.validateTruckCapacity("м3", volumeInCubicMeters, safeTruck)
            if (volumeError != null) return@updateShipmentHandler volumeError
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
                    "Error" to ErrorMessages.SHIPMENT_NOT_FOUND,
                ),
            )
        }
    }
