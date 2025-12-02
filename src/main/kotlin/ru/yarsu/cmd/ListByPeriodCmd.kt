package ru.yarsu.cmd

import com.beust.jcommander.ParametersDelegate
import com.fasterxml.jackson.core.JsonFactory
import ru.yarsu.args.PeriodArgs
import ru.yarsu.data.Shipment
import java.io.StringWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ListByPeriodCmd : BaseCmd() {
    @ParametersDelegate
    val periodArgs = PeriodArgs()
    private val fromDate: LocalDate
        get() = LocalDate.parse(requireNotNull(periodArgs.periodFromArgs))

    private val toDate: LocalDate
        get() = LocalDate.parse(requireNotNull(periodArgs.periodToArgs))

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun convertTaskListByPeriodToJSON(shipments: List<Shipment>): String {
        val shipment = sortByDateAndId(shipments)
        val factory = JsonFactory()
        val writer = StringWriter()
        val jsonGenerator = factory.createGenerator(writer)

        jsonGenerator.use { it ->
            it.useDefaultPrettyPrinter()
            it.writeStartObject()
            it.writeStringField("from", fromDate.toString())
            it.writeStringField("to", toDate.toString())
            it.writeFieldName("report")
            it.writeStartArray()
            shipment.forEach { t ->
                it.writeStartObject()
                it.writeStringField("Id", t.id.toString())
                it.writeStringField("Title", t.title)
                it.writeStringField("ShipmentDate", t.shipmentDateTime.toLocalDate().format(dateFormatter))
                it.writeNumberField("Cost", t.cost)
                it.writeEndObject()
            }
            it.writeEndArray()
            it.writeEndObject()
        }
        return writer.toString()
    }

    private fun sortByDateAndId(shipments: List<Shipment>): List<Shipment> {
        if (fromDate.isAfter(toDate)) {
            throw IllegalArgumentException("Параметр --from не может быть больше параметра --to")
        }

        return shipments
            .filter { shipment ->
                val date = shipment.shipmentDateTime.toLocalDate()
                !date.isBefore(fromDate) && !date.isAfter(toDate)
            }.sortedWith(compareBy({ it.shipmentDateTime }, { it.id }))
    }
}
