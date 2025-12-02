package ru.yarsu.cmd

import com.beust.jcommander.ParametersDelegate
import com.fasterxml.jackson.core.JsonFactory
import ru.yarsu.args.YearArgs
import ru.yarsu.data.Shipment
import java.io.StringWriter
import java.math.BigDecimal
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

class ReportCmd : BaseCmd() {
    @ParametersDelegate
    val yearsArgs = YearArgs()
    val year get() = yearsArgs.yearArgs

    fun convertTaskReportToJSON(shipments: List<Shipment>): String {
        val shipmentByMonth =
            shipments
                .filter { it.shipmentDateTime.year == year }
                .groupBy { it.shipmentDateTime.monthValue }

        val factory = JsonFactory()
        val writer = StringWriter()
        val jsonGenerator = factory.createGenerator(writer)

        jsonGenerator.use { gen ->
            gen.useDefaultPrettyPrinter()
            gen.writeStartObject()
            gen.writeFieldName("report")
            gen.writeStartArray()

            shipmentByMonth.toSortedMap().forEach { (month, list) ->
                val monthTitle =
                    Month
                        .of(month)
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru"))
                        .replaceFirstChar { it.uppercase() }

                val count = list.size

                val cost: BigDecimal = list.fold(BigDecimal.ZERO) { acc, item -> acc + item.cost }

                val weight =
                    list
                        .filter { it.measure == "т" }
                        .sumOf { it.count.toDouble() }

                val volume =
                    list
                        .filter { it.measure == "м3" }
                        .sumOf { it.count.toDouble() }

                gen.writeStartObject()
                gen.writeNumberField("month", month)
                gen.writeStringField("monthTitle", monthTitle)
                gen.writeNumberField("count", count)
                gen.writeNumberField("cost", cost)
                gen.writeNumberField("weight", weight)
                gen.writeNumberField("volume", volume)
                gen.writeEndObject()
            }

            gen.writeEndArray()
            gen.writeEndObject()
        }

        return writer.toString()
    }
}
