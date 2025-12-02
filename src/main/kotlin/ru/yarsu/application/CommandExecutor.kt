package ru.yarsu.application

import ru.yarsu.cmd.ListByPeriodCmd
import ru.yarsu.cmd.ListBySwgCmd
import ru.yarsu.cmd.ListCmd
import ru.yarsu.cmd.ReportCmd
import ru.yarsu.cmd.ShowShipmentCmd
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
