package ru.yarsu.web.routes.v1

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.PaginationUtils
import ru.yarsu.web.routes.util.SortingUtils
import ru.yarsu.web.routes.validateV1.QueryValidation

fun shipmentsListPagedHandler(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        try {
            val pageRaw = request.query("page")
            val recordsRaw = request.query("records-per-page")

            val pageValidation = if (pageRaw != null) QueryValidation.validatePage(pageRaw) else null
            val recordsValidation = if (recordsRaw != null) QueryValidation.validateRecordsPerPage(recordsRaw) else null

            when {
                pageValidation != null -> pageValidation
                recordsValidation != null -> recordsValidation
                else -> {
                    val page = pageRaw?.toInt() ?: 1
                    val recordsPerPage = recordsRaw?.toInt() ?: 10

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
            }
        } catch (e: Exception) {
            GetResponse.responseBadRequest("Неизвестная ошибка")
        }
    }
