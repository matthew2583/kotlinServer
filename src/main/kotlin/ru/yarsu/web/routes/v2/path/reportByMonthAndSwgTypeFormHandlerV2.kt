package ru.yarsu.web.routes.v2.path

import org.http4k.core.HttpHandler
import org.http4k.lens.LensFailure
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.webForm
import org.http4k.core.Body
import org.http4k.lens.int
import org.http4k.lens.string
import ru.yarsu.internal.SwgType
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.web.routes.util.GetResponse
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private val yearFormLens = FormField.int().required("year", "Год обязателен")
private val swgTypeFormLens = FormField.string().required("swgType", "Тип ПГС обязателен")

private val reportFormBodyLens =
    Body.webForm(
        Validator.Strict,
        yearFormLens,
        swgTypeFormLens
    ).toLens()

fun reportByMonthAndSwgTypeFormHandlerV2(shipmentStorage: ShipmentStorage): HttpHandler =
    reportByMonthAndSwgTypeFormHandlerV2@
    { request ->
        try {
            val form = reportFormBodyLens(request)
            val year = yearFormLens(form)
            val swgTypeString = swgTypeFormLens(form)

            if (year < 2000) {
                return@reportByMonthAndSwgTypeFormHandlerV2 GetResponse.responseBadRequest(
                    mapOf("Error" to "Год должен быть не менее 2000")
                )
            }

            val swgType = SwgType.entries.find { it.displayName == swgTypeString }
            if (swgType == null) {
                return@reportByMonthAndSwgTypeFormHandlerV2 GetResponse.responseBadRequest(
                    mapOf(
                        "Error" to "Неизвестный тип ПГС: $swgTypeString",
                        "AvailableTypes" to SwgType.entries.map { it.displayName }
                    )
                )
            }

            val monthShipments = shipmentStorage.getShipmentsByMonth(year)

            val allMonthsResponse = (1..12).map { month ->
                val shipmentsForMonth = monthShipments[month] ?: emptyList()
                val filteredShipments = shipmentsForMonth.filter { it.swg == swgType }

                val monthTitle =
                    Month
                        .of(month)
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"))
                        .replaceFirstChar { it.uppercaseChar() }

                val weight = filteredShipments.filter { it.measure == "т" }.sumOf { it.count.toDouble() }

                filteredShipments.forEach { shipment ->

                    shipmentStorage.updateShipmentTitle(shipment.id, "учтено")
                }

                mapOf(
                    "Month" to month,
                    "Count" to filteredShipments.size,
                    "Weight" to weight,
                )
            }

            val totalShipmentCount = allMonthsResponse.sumOf { it["Count"] as Int }

            val response = mapOf(
                "Shipments" to allMonthsResponse,
                "ShipmentCount" to totalShipmentCount
            )

            GetResponse.responseOK(response)
        } catch (e: LensFailure) {
            GetResponse.responseBadRequest(
                mapOf("Error" to "Некорректные данные формы: ${e.message}")
            )
        }
    }
