package ru.yarsu.web.routes.v2.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.lensValidate.path.trucksIdLens
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils

fun dumpTrucksIDListPagedHandlerV2(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    { request ->
        val id = trucksIdLens(request)
        val trucks = trucksStorage.getTruckById(id)
        when {
            trucks == null ->
                GetResponse.responseNotFound(
                    mapOf(
                        "DumpTruckId" to id.toString(),
                        "Error" to "Самосвал не найден",
                    ),
                )

            else -> {
                val shipments = shipmentStorage.getShipmentsByTruckId(trucks.id)
                val sorted = SortingUtils.sortShipmentsByDateTimeDesc(shipments)
                val responseData =
                    mapOf(
                        "Id" to trucks.id.toString(),
                        "Model" to trucks.model,
                        "Registration" to trucks.registration,
                        "Capacity" to trucks.capacity,
                        "Volume" to trucks.volume,
                        "Shipments" to
                            sorted.map {
                                mapOf(
                                    "Id" to it.id.toString(),
                                    "Title" to it.title,
                                    "ShipmentDateTime" to it.shipmentDateTime.toString(),
                                )
                            },
                    )
                GetResponse.responseOK(responseData)
            }
        }
    }
