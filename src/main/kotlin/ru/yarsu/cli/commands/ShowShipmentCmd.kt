package ru.yarsu.cli.commands

import com.beust.jcommander.ParametersDelegate
import ru.yarsu.cli.CliUtils
import ru.yarsu.cli.args.ShipmentIdArgs
import ru.yarsu.data.Shipment
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
            val data =
                mapOf(
                    "shipment-id" to (id ?: ""),
                    "shipment" to
                        mapOf(
                            "Id" to shipment.id.toString(),
                            "Title" to shipment.title,
                            "SWG" to shipment.swg.toString(),
                            "Measure" to shipment.measure,
                            "Count" to shipment.count.toDouble(),
                            "Price" to shipment.price.toDouble(),
                            "Cost" to shipment.cost.toDouble(),
                            "ShipmentDateTime" to shipment.shipmentDateTime.toString(),
                            "Washing" to shipment.washing,
                        ),
                )
            CliUtils.toJson(data)
        } catch (ex: Exception) {
            println(ex.message)
            exitProcess(1)
        }

    private fun findById(shipments: List<Shipment>): Shipment =
        shipments.find { it.id.toString() == id }
            ?: throw Exception("UUID не найден")
}
