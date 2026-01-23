package ru.yarsu.cli.commands

import com.beust.jcommander.ParametersDelegate
import ru.yarsu.cli.CliUtils
import ru.yarsu.cli.args.PeriodArgs
import ru.yarsu.data.Shipment
import java.time.LocalDate

class ListByPeriodCmd : BaseCmd() {
    @ParametersDelegate
    val periodArgs = PeriodArgs()
    private val fromDate: LocalDate
        get() = LocalDate.parse(requireNotNull(periodArgs.periodFromArgs))

    private val toDate: LocalDate
        get() = LocalDate.parse(requireNotNull(periodArgs.periodToArgs))

    fun convertTaskListByPeriodToJSON(shipments: List<Shipment>): String {
        val filtered = filterByPeriod(shipments)
        val sorted = CliUtils.sortByDateTimeAsc(filtered)
        val report =
            sorted.map {
                mapOf(
                    "Id" to it.id.toString(),
                    "Title" to it.title,
                    "ShipmentDate" to it.shipmentDateTime.toLocalDate().toString(),
                    "Cost" to it.cost,
                )
            }
        return CliUtils.toJson(mapOf("from" to fromDate.toString(), "to" to toDate.toString(), "report" to report))
    }

    private fun filterByPeriod(shipments: List<Shipment>): List<Shipment> {
        if (fromDate.isAfter(toDate)) {
            throw IllegalArgumentException("Параметр --from не может быть больше параметра --to")
        }
        return shipments.filter {
            val date = it.shipmentDateTime.toLocalDate()
            !date.isBefore(fromDate) && !date.isAfter(toDate)
        }
    }
}
