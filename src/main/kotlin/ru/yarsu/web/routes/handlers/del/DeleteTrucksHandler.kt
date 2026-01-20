package ru.yarsu.web.routes.handlers.del

import org.http4k.core.HttpHandler
import org.http4k.lens.LensFailure
import ru.yarsu.domain.Roles
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.lens.path.trucksIdLens
import ru.yarsu.web.routes.util.AuthUtils
import ru.yarsu.web.routes.util.ErrorMessages
import ru.yarsu.web.routes.util.GetResponse

fun deleteTrucksHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    deleteTruck@{ request ->

        val (_, errorResponse) = AuthUtils.requireRole(request, Roles.Manager)
        if (errorResponse != null) return@deleteTruck errorResponse

        val id =
            try {
                trucksIdLens(request)
            } catch (_: LensFailure) {
                return@deleteTruck GetResponse.responseBadRequest(mapOf("Error" to ErrorMessages.INVALID_TRUCK_ID))
            }

        val truck =
            trucksStorage.getTruckById(id)
                ?: return@deleteTruck GetResponse.responseNotFound(
                    mapOf(
                        "DumpTruckId" to id.toString(),
                        "Error" to ErrorMessages.TRUCK_NOT_FOUND,
                    ),
                )

        val hasShipments = shipmentStorage.hasShipmentsByDumpTruck(truck.id)
        if (hasShipments) {
            return@deleteTruck GetResponse.responseForbidden(
                mapOf("Error" to "Самосвал не может быть удалён, так как ему соответствуют акты отгрузки"),
            )
        }

        trucksStorage.deleteTruck(truck.id)
        GetResponse.responseNoContent()
    }
