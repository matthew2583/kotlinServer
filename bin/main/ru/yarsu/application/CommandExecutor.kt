package ru.yarsu.application

import ru.yarsu.cli.commands.ListByPeriodCmd
import ru.yarsu.cli.commands.ListBySwgCmd
import ru.yarsu.cli.commands.ListCmd
import ru.yarsu.cli.commands.ReportCmd
import ru.yarsu.cli.commands.ShowShipmentCmd
import ru.yarsu.data.Shipment

class CommandExecutor(
    private val listCmd: ListCmd,
    private val showShipmentCmd: ShowShipmentCmd,
    private val listBySwgCmd: ListBySwgCmd,
    private val listByPeriodCmd: ListByPeriodCmd,
    private val reportCmd: ReportCmd,
) {
    fun executeCommand(
        command: String,
        shipments: List<Shipment>,
    ): String =
        when (command) {
            "list" -> listCmd.convertTaskListToJSON(shipments)
            "show-shipment" -> showShipmentCmd.convertTaskShowShipmentToJSON(shipments)
            "list-by-swg" -> listBySwgCmd.convertTaskListBySwgToJSON(shipments)
            "list-by-period" -> listByPeriodCmd.convertTaskListByPeriodToJSON(shipments)
            "report" -> reportCmd.convertTaskReportToJSON(shipments)
            else -> throw IllegalArgumentException("Неизвестная команда: $command")
        }
}
