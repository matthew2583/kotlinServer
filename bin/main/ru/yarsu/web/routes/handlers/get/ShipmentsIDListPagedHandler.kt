package ru.yarsu.web.routes.handlers.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.lens.path.shipmentIdLens
import ru.yarsu.web.routes.util.ErrorMessages
import ru.yarsu.web.routes.util.GetResponse

fun shipmentsIDListHandler(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
): HttpHandler =
    { request ->
        val id = shipmentIdLens(request)
        val shipment = shipmentStorage.getShipmentById(id)

        when (shipment) {
            null -> {
                GetResponse.responseNotFound(
                    mapOf(
                        "ShipmentId" to id.toString(),
                        "Error" to ErrorMessages.SHIPMENT_NOT_FOUND,
                    ),
                )
            }

            else -> {
                val truck = trucksStorage.getTruckById(shipment.dumpTruck)
                val manager = employeesStorage.getEmployeesById(shipment.manager)

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
                        "Model" to truck?.model,
                        "Registration" to truck?.registration,
                        "Washing" to shipment.washing,
                        "ManagerEmail" to manager?.email,
                    )

                GetResponse.responseOK(responseData)
            }
        }
    }
