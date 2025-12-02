package ru.yarsu.web.routes.v1

import org.http4k.core.HttpHandler
import org.http4k.routing.path
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils
import ru.yarsu.web.routes.validateV1.PathValidation

fun dumpTrucksIDListPagedHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    { req ->
        try {
            val trucksIdRaw = req.path("dump-truck-id")
            val (trucksIdError, trucksUuid) = PathValidation.validateId(trucksIdRaw)

            when {
                trucksIdError != null -> trucksIdError
                else -> {
                    val trucks = trucksStorage.getTruckById(trucksUuid)
                    when {
                        trucks == null ->
                            GetResponse.responseNotFound(
                                mapOf(
                                    "DumpTruckId" to trucksIdRaw.toString(),
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
            }
        } catch (e: Exception) {
            GetResponse.responseBadRequest("Неизвестная ошибка: ${e.message}")
        }
    }
