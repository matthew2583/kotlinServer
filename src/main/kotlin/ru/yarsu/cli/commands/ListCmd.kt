package ru.yarsu.cli.commands

import ru.yarsu.cli.CliUtils
import ru.yarsu.data.Shipment

class ListCmd : BaseCmd() {
    fun convertTaskListToJSON(shipments: List<Shipment>): String {
        val sorted = CliUtils.sortByDateTimeAsc(shipments)
        val report = sorted.map { mapOf("Id" to it.id.toString(), "Title" to it.title, "Cost" to it.cost) }
        return CliUtils.toJson(mapOf("report" to report))
    }
}
