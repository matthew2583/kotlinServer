package ru.yarsu.web.routes.v3.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.lensValidate.query.paginationLens
import ru.yarsu.web.routes.lensValidate.query.swgTypeLens
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.PaginationUtils
import ru.yarsu.web.routes.util.SortingUtils

fun bySwgTypeListPagedHandlerV3(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        val swgType = swgTypeLens(request)
        val (page, recordsPerPage) = paginationLens(request)

        val filtered = shipmentStorage.getShipmentsBySwgType(swgType)
        val sorted = SortingUtils.sortShipmentsByDateTimeDesc(filtered)
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
