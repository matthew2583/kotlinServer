package ru.yarsu.web.routes.v2.del

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.lensValidate.path.trucksIdLens
import ru.yarsu.web.routes.util.GetResponse

fun deleteTrucksHandlerV2(
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
                val hasShipments = shipmentStorage.hasShipmentsByDumpTruck(id)

                if (hasShipments) {
                    GetResponse.responseForbidden(
                        mapOf(
                            "Error" to "Самосвал не может быть удалён, так как ему соответствуют акты отгрузки",
                        ),
                    )
                } else {
                    trucksStorage.deleteTruck(id)
                    GetResponse.responseNoContent()
                }
            }
        }
    }
