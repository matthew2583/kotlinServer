package ru.yarsu

import com.beust.jcommander.JCommander
import ru.yarsu.application.CommandExecutor
import ru.yarsu.application.WebServer
import ru.yarsu.args.PathArgs
import ru.yarsu.args.PortArgs
import ru.yarsu.cmd.ListByPeriodCmd
import ru.yarsu.cmd.ListBySwgCmd
import ru.yarsu.cmd.ListCmd
import ru.yarsu.cmd.ReportCmd
import ru.yarsu.cmd.ShowShipmentCmd
import ru.yarsu.internal.CsvParser
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val (pathArgs, portArgs, command) = parseArguments(args)
        val csvParser = CsvParser()

        if (command == null) {
            startWebServer(pathArgs, portArgs, csvParser)
        } else {
            executeCommand(command, pathArgs, csvParser)
        }
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
        exitProcess(1)
    }
}

private fun parseArguments(args: Array<String>): Triple<PathArgs, PortArgs, String?> {
    val pathArgs = PathArgs()
    val portArgs = PortArgs()

    val jCommander =
        JCommander
            .newBuilder()
            .addObject(pathArgs)
            .addObject(portArgs)
            .addCommand("list", ListCmd())
            .addCommand("show-shipment", ShowShipmentCmd())
            .addCommand("list-by-swg", ListBySwgCmd())
            .addCommand("list-by-period", ListByPeriodCmd())
            .addCommand("report", ReportCmd())
            .build()

    try {
        jCommander.parse(*args)
    } catch (e: Exception) {
        println("Ошибка парсинга аргументов: ${e.message}")
        jCommander.usage()
        exitProcess(1)
    }

    return Triple(pathArgs, portArgs, jCommander.parsedCommand)
}

private fun validateAndGetFiles(pathArgs: PathArgs): Triple<String, String, String> {
    val shipmentsFile = pathArgs.swgFile
    val trucksFile = pathArgs.dumpTrucksFile
    val employeesFile = pathArgs.employeesFile

    if (shipmentsFile.isNullOrBlank() || trucksFile.isNullOrBlank() || employeesFile.isNullOrBlank()) {
        println("Ошибка: обязательно укажите --shipments-file, --trucks-file и --employees-file")
        exitProcess(1)
    }

    return Triple(shipmentsFile, trucksFile, employeesFile)
}

private fun startWebServer(
    pathArgs: PathArgs,
    portArgs: PortArgs,
    csvParser: CsvParser,
) {
    val (shipmentsFile, trucksFile, employeesFile) = validateAndGetFiles(pathArgs)

    val shipmentList = csvParser.shipmentParser(File(shipmentsFile))
    val trucksList = csvParser.trucksParser(File(trucksFile))
    val employeesList = csvParser.employeesParser(File(employeesFile))

    val shipmentStorage = ShipmentStorage(shipmentList)
    val trucksStorage = TrucksStorage(trucksList)
    val employeesStorage = EmployeesStorage(employeesList)

    val webServer = WebServer()
    webServer.start(
        shipmentStorage,
        trucksStorage,
        employeesStorage,
        portArgs.ports,
        shipmentsFile,
        trucksFile,
        employeesFile,
    )
}

private fun executeCommand(
    command: String,
    pathArgs: PathArgs,
    csvParser: CsvParser,
) {
    val (shipmentsFile, _, _) = validateAndGetFiles(pathArgs)
    val shipmentList = csvParser.shipmentParser(File(shipmentsFile))

    val commandExecutor =
        CommandExecutor(
            ListCmd(),
            ShowShipmentCmd(),
            ListBySwgCmd(),
            ListByPeriodCmd(),
            ReportCmd(),
        )

    val result = commandExecutor.executeCommand(command, shipmentList)
    println(result)
}
