package ru.yarsu.web.routes.handlers.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.lens.query.paginationLens
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.PaginationUtils
import ru.yarsu.web.routes.util.ResponseMappers
import ru.yarsu.web.routes.util.SortingUtils

fun shipmentsListPagedHandler(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        val (page, recordsPerPage) = paginationLens(request)

        val allShipments = shipmentStorage.getAllShipments()
        val sorted = SortingUtils.sortShipmentsByDateTime(allShipments)
        val data = PaginationUtils.applyPaging(sorted, page, recordsPerPage)

        val responseData = data.map(ResponseMappers::shipmentToBasicMap)
        GetResponse.responseOK(responseData)
    }
