package ru.yarsu.cmd

import com.beust.jcommander.ParametersDelegate
import com.fasterxml.jackson.core.JsonFactory
import ru.yarsu.args.SwgTypeArgs
import ru.yarsu.data.Shipment
import ru.yarsu.internal.SwgType
import java.io.StringWriter

class ListBySwgCmd : BaseCmd() {
    @ParametersDelegate
    val swgTypes = SwgTypeArgs()
    val type: SwgType? get() = swgTypes.swgType

    fun convertTaskListBySwgToJSON(shipments: List<Shipment>): String {
        val shipment = sortSwgListByDateAndId(shipments)
        val factory = JsonFactory()
        val writer = StringWriter()
        val jsonGenerator = factory.createGenerator(writer)

        jsonGenerator.use { it ->
            it.useDefaultPrettyPrinter()
            it.writeStartObject()
            it.writeStringField("type", type.toString())
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

    fun sortSwgListByDateAndId(shipments: List<Shipment>): List<Shipment> {
        val filteredList = shipments.filter { it.swg == type }
        return filteredList.sortedWith(compareByDescending<Shipment> { it.shipmentDateTime }.thenBy { it.id })
    }
}
