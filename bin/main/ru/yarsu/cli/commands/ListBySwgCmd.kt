package ru.yarsu.cli.commands

import com.beust.jcommander.ParametersDelegate
import ru.yarsu.cli.CliUtils
import ru.yarsu.cli.args.SwgTypeArgs
import ru.yarsu.data.Shipment
import ru.yarsu.domain.SwgType

class ListBySwgCmd : BaseCmd() {
    @ParametersDelegate
    val swgTypes = SwgTypeArgs()
    val type: SwgType? get() = swgTypes.swgType

    fun convertTaskListBySwgToJSON(shipments: List<Shipment>): String {
        val filtered = shipments.filter { it.swg == type }
        val sorted = CliUtils.sortByDateTimeDesc(filtered)
        val report = sorted.map { mapOf("Id" to it.id.toString(), "Title" to it.title, "Cost" to it.cost) }
        return CliUtils.toJson(mapOf("type" to type.toString(), "report" to report))
    }
}
