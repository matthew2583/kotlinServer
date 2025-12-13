package ru.yarsu.web.routes.v3.get

import org.http4k.core.HttpHandler
import org.http4k.lens.LensFailure
import ru.yarsu.internal.Roles
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.filter.AuthKeys
import ru.yarsu.web.routes.lensValidate.path.trucksIdLens
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils

fun dumpTrucksIDListPagedHandlerV3(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    dumpTruck@{ request ->

        val role =
            AuthKeys.employeeRoleKey(request)
                ?: return@dumpTruck GetResponse.responseUnauthorized(mapOf("Error" to "Отказанно в авторизации"))

        if (role != Roles.Employee && role != Roles.Manager) {
            return@dumpTruck GetResponse.responseUnauthorized(mapOf("Error" to "Отказанно в авторизации"))
        }

        val id =
            try {
                trucksIdLens(request)
            } catch (_: LensFailure) {
                return@dumpTruck GetResponse.responseBadRequest(
                    mapOf("Error" to "Некорректный идентификатор самосвала: dump-truck-id должен быть UUID"),
                )
            }

        val truck =
            trucksStorage.getTruckById(id)
                ?: return@dumpTruck GetResponse.responseNotFound(
                    mapOf(
                        "DumpTruckId" to id.toString(),
                        "Error" to "Самосвал не найден",
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
