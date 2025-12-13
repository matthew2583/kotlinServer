package ru.yarsu.web.routes.v3.del

import org.http4k.core.HttpHandler
import org.http4k.lens.LensFailure
import ru.yarsu.internal.Roles
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.filter.AuthKeys
import ru.yarsu.web.routes.lensValidate.path.trucksIdLens
import ru.yarsu.web.routes.util.GetResponse

fun deleteTrucksHandlerV3(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
): HttpHandler =
    deleteTruck@{ request ->

        val role =
            AuthKeys.employeeRoleKey(request)
                ?: return@deleteTruck GetResponse.responseUnauthorized(mapOf("Error" to "Отказанно в авторизации"))

        if (role != Roles.Manager) {
            return@deleteTruck GetResponse.responseUnauthorized(mapOf("Error" to "Отказанно в авторизации"))
        }

        val id =
            try {
                trucksIdLens(request)
            } catch (_: LensFailure) {
                return@deleteTruck GetResponse.responseBadRequest(
                    mapOf("Error" to "Некорректный идентификатор самосвала"),
                )
            }

        val truck =
            trucksStorage.getTruckById(id)
                ?: return@deleteTruck GetResponse.responseNotFound(
                    mapOf(
                        "DumpTruckId" to id.toString(),
                        "Error" to "Самосвал не найден",
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
