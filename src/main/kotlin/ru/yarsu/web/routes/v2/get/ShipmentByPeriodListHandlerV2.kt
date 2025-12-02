package ru.yarsu.web.routes.v2.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.lensValidate.query.paginationLens
import ru.yarsu.web.routes.lensValidate.query.periodLens
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.PaginationUtils
import ru.yarsu.web.routes.util.SortingUtils

fun shipmentByPeriodListHandlerV2(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        val (page, recordsPerPage) = paginationLens(request)
        val (from, to) = periodLens(request)

        val filtered = shipmentStorage.getShipmentsByPeriod(from, to)
        val sorted = SortingUtils.sortShipmentsByDateTime(filtered)
        val data = PaginationUtils.applyPaging(sorted, page, recordsPerPage)

        val responseData =
            data.map {
                mapOf(
                    "Id" to it.id.toString(),
                    "Title" to it.title,
                    "ShipmentDate" to it.shipmentDateTime.toLocalDate().toString(),
                    "Cost" to it.cost,
                )
            }
        GetResponse.responseOK(responseData)
    }
