package ru.yarsu.web.routes.v1

import org.http4k.core.HttpHandler
import org.http4k.routing.path
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.validateV1.PathValidation

fun shipmentsIDListHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    { req ->
        try {
            val idRaw = req.path("shipment-id")
            val (idError, shipmentId) = PathValidation.validateId(idRaw)

            when {
                idError != null -> idError
                else -> {
                    val shipment = shipmentStorage.getShipmentById(shipmentId)
                    when {
                        shipment == null ->
                            GetResponse.responseNotFound(
                                mapOf(
                                    "ShipmentId" to idRaw.toString(),
                                    "Error" to "Акт отгрузки ПГС не найден",
                                ),
                            )
                        else -> {
                            val truck = trucksStorage.getTruckById(shipment.dumpTruck)
                            val responseData =
                                mapOf(
                                    "Id" to shipment.id.toString(),
                                    "Title" to shipment.title,
                                    "SWG" to shipment.swg.displayName,
                                    "Measure" to shipment.measure,
                                    "Count" to shipment.count,
                                    "Price" to shipment.price,
                                    "Cost" to shipment.cost,
                                    "ShipmentDateTime" to shipment.shipmentDateTime.toString(),
                                    "Model" to (truck?.model ?: ""),
                                    "Registration" to (truck?.registration ?: ""),
                                    "Washing" to shipment.washing,
                                )
                            GetResponse.responseOK(responseData)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            GetResponse.responseBadRequest("Неизвестная ошибка: ${e.message}")
        }
    }
