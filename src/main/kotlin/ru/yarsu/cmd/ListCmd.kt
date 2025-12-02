package ru.yarsu.cmd

import com.fasterxml.jackson.core.JsonFactory
import ru.yarsu.data.Shipment
import java.io.StringWriter

class ListCmd : BaseCmd() {
    fun convertTaskListToJSON(shipments: List<Shipment>): String {
        val shipment = sortSwgList(shipments)
        val factory = JsonFactory()
        val writer = StringWriter()
        val jsonGenerator = factory.createGenerator(writer)

        jsonGenerator.use { it ->
            it.useDefaultPrettyPrinter()
            it.writeStartObject()
            it.writeFieldName("report")
            it.writeStartArray()
            shipment.forEach { t ->
                it.writeStartObject()
                it.writeStringField("Id", t.id.toString())
                it.writeStringField("Title", t.title)
                it.writeNumberField("Cost", t.cost)
                it.writeEndObject()
            }
            it.writeEndArray()
            it.writeEndObject()
        }
        return writer.toString()
    }

    fun sortSwgList(shipment: List<Shipment>): List<Shipment> =
        shipment.sortedWith(
            compareBy(
                { it.shipmentDateTime },
                { it.id },
            ),
        )
}
