package ru.yarsu.web.routes.v1

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.PaginationUtils
import ru.yarsu.web.routes.util.SortingUtils
import ru.yarsu.web.routes.validateV1.QueryValidation

fun shipmentByPeriodListHandler(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        try {
            val fromStr = request.query("from")
            val toStr = request.query("to")
            val pageRaw = request.query("page")
            val recordsRaw = request.query("records-per-page")

            val pageValidation = if (pageRaw != null) QueryValidation.validatePage(pageRaw) else null
            val recordsValidation = if (recordsRaw != null) QueryValidation.validateRecordsPerPage(recordsRaw) else null
            val (dateErr, dateRange) = QueryValidation.validateToPeriod(fromStr, toStr)

            when {
                pageValidation != null -> pageValidation
                recordsValidation != null -> recordsValidation
                dateErr != null -> dateErr
                dateRange != null -> {
                    val (from, to) = dateRange

                    val page = pageRaw?.toInt() ?: 1
                    val recordsPerPage = recordsRaw?.toInt() ?: 10

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
                else -> {
                    GetResponse.responseBadRequest("Неизвестная ошибка")
                }
            }
        } catch (e: Exception) {
            GetResponse.responseBadRequest("Неизвестная ошибка")
        }
    }
