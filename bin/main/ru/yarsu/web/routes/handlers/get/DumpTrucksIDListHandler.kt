package ru.yarsu.web.routes.handlers.get

import org.http4k.core.HttpHandler
import org.http4k.lens.LensFailure
import ru.yarsu.domain.Roles
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.lens.path.trucksIdLens
import ru.yarsu.web.routes.util.AuthUtils
import ru.yarsu.web.routes.util.ErrorMessages
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils

fun dumpTrucksIDListPagedHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    dumpTruck@{ request ->

        val (_, errorResponse) = AuthUtils.requireRole(request, Roles.Employee, Roles.Manager)
        if (errorResponse != null) return@dumpTruck errorResponse

        val id =
            try {
                trucksIdLens(request)
            } catch (_: LensFailure) {
                return@dumpTruck GetResponse.responseBadRequest(mapOf("Error" to ErrorMessages.INVALID_TRUCK_ID))
            }

        val truck =
            trucksStorage.getTruckById(id)
                ?: return@dumpTruck GetResponse.responseNotFound(
                    mapOf(
                        "DumpTruckId" to id.toString(),
                        "Error" to ErrorMessages.TRUCK_NOT_FOUND,
                    ),
                )

        val shipments = shipmentStorage.getShipmentsByTruckId(truck.id)
        val sorted = SortingUtils.sortShipmentsByDateTimeDesc(shipments)

        val responseData =
            mapOf(
                "Id" to truck.id.toString(),
                "Model" to truck.model,
                "Registration" to truck.registration,
                "Capacity" to truck.capacity,
                "Volume" to truck.volume,
                "Shipments" to
                    sorted.map { s ->
                        mapOf(
                            "Id" to s.id.toString(),
                            "Title" to s.title,
                            "ShipmentDateTime" to s.shipmentDateTime.toString(),
                        )
                    },
            )

        GetResponse.responseOK(responseData)
    }
