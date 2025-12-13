package ru.yarsu.web.routes.v3.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.lensValidate.query.paginationLens
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.PaginationUtils
import ru.yarsu.web.routes.util.SortingUtils

fun shipmentsListPagedHandlerV3(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        val (page, recordsPerPage) = paginationLens(request)

        val allShipments = shipmentStorage.getAllShipments()
        val sorted = SortingUtils.sortShipmentsByDateTime(allShipments)
        val data = PaginationUtils.applyPaging(sorted, page, recordsPerPage)

        val responseData =
            data.map {
                mapOf(
                    "Id" to it.id.toString(),
                    "Title" to it.title,
                    "Cost" to it.cost,
                )
            }
        GetResponse.responseOK(responseData)
    }
