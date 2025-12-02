package ru.yarsu.cmd

import com.beust.jcommander.ParametersDelegate
import com.fasterxml.jackson.core.JsonFactory
import ru.yarsu.args.ShipmentIdArgs
import ru.yarsu.data.Shipment
import java.io.StringWriter
import kotlin.system.exitProcess

class ShowShipmentCmd : BaseCmd() {
    @ParametersDelegate
    val shipmentID = ShipmentIdArgs()
    val id: String? get() = shipmentID.shipmentID

    fun convertTaskShowShipmentToJSON(shipments: List<Shipment>): String =
        try {
            if (shipments.isEmpty()) {
                println("В файле только заголовки, данные отсутствуют")
                exitProcess(1)
            }
            val shipment = findById(shipments)
            val factory = JsonFactory()
            val writer = StringWriter()
            val jsonGenerator = factory.createGenerator(writer)

            jsonGenerator.use {
                it.useDefaultPrettyPrinter()
                it.writeStartObject()
                it.writeStringField("shipment-id", id ?: "")
                it.writeFieldName("shipment")
                it.writeStartObject()
                it.writeStringField("Id", shipment.id.toString())
                it.writeStringField("Title", shipment.title)
                it.writeStringField("SWG", shipment.swg.toString())
                it.writeStringField("Measure", shipment.measure)
                it.writeNumberField("Count", shipment.count.toDouble())
                it.writeNumberField("Price", shipment.price.toDouble())
                it.writeNumberField("Cost", shipment.cost.toDouble())
                it.writeStringField("ShipmentDateTime", shipment.shipmentDateTime.toString())
                it.writeBooleanField("Washing", shipment.washing)
                it.writeEndObject()
                it.writeEndObject()
            }

            writer.toString()
        } catch (ex: Exception) {
            println(ex.message)
            exitProcess(1)
        }

    fun findById(shipment: List<Shipment>): Shipment =
        shipment.find { it.id.toString() == id }
            ?: throw Exception("UUID не найден")
}
