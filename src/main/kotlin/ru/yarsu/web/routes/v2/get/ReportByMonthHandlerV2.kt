package ru.yarsu.web.routes.v2.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.lensValidate.query.yearLens
import ru.yarsu.web.routes.util.GetResponse
import java.math.BigDecimal
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

fun reportByMonthHandlerV2(shipmentStorage: ShipmentStorage): HttpHandler =
    { request ->
        val year = yearLens(request)

        val monthShipments = shipmentStorage.getShipmentsByMonth(year)

        val responseData =
            monthShipments
                .filterValues { it.isNotEmpty() }
                .toSortedMap()
                .map { (month, list) ->
                    val monthTitle =
                        Month
                            .of(month)
                            .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"))
                            .replaceFirstChar { it.uppercaseChar() }
                    val cost = list.fold(BigDecimal.ZERO) { acc, item -> acc + item.cost }
                    val weight = list.filter { it.measure == "т" }.sumOf { it.count.toDouble() }
                    val volume = list.filter { it.measure == "м3" }.sumOf { it.count.toDouble() }

                    mapOf(
                        "Month" to month,
                        "MonthTitle" to monthTitle,
                        "Count" to list.size,
                        "Cost" to cost,
                        "Weight" to weight,
                        "Volume" to volume,
                    )
                }

        GetResponse.responseOK(responseData)
    }
