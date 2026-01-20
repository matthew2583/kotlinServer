package ru.yarsu.cli.commands

import com.beust.jcommander.ParametersDelegate
import ru.yarsu.cli.CliUtils
import ru.yarsu.cli.args.YearArgs
import ru.yarsu.data.Shipment
import java.math.BigDecimal
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

class ReportCmd : BaseCmd() {
    @ParametersDelegate
    val yearsArgs = YearArgs()
    val year get() = yearsArgs.yearArgs

    fun convertTaskReportToJSON(shipments: List<Shipment>): String {
        val byMonth =
            shipments
                .filter { it.shipmentDateTime.year == year }
                .groupBy { it.shipmentDateTime.monthValue }

        val report =
            byMonth.toSortedMap().map { (month, list) ->
                val monthTitle =
                    Month
                        .of(month)
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"))
                        .replaceFirstChar { it.uppercase() }
                val cost: BigDecimal = list.fold(BigDecimal.ZERO) { acc, item -> acc + item.cost }
                val weight = list.filter { it.measure == "т" }.sumOf { it.count.toDouble() }
                val volume = list.filter { it.measure == "м3" }.sumOf { it.count.toDouble() }

                mapOf(
                    "month" to month,
                    "monthTitle" to monthTitle,
                    "count" to list.size,
                    "cost" to cost,
                    "weight" to weight,
                    "volume" to volume,
                )
            }
        return CliUtils.toJson(mapOf("report" to report))
    }
}
