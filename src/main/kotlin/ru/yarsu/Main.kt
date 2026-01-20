package ru.yarsu

import com.beust.jcommander.JCommander
import ru.yarsu.application.CommandExecutor
import ru.yarsu.application.WebServer
import ru.yarsu.auth.JwtTools
import ru.yarsu.cli.args.KeyArgs
import ru.yarsu.cli.args.PathArgs
import ru.yarsu.cli.args.PortArgs
import ru.yarsu.cli.commands.ListByPeriodCmd
import ru.yarsu.cli.commands.ListBySwgCmd
import ru.yarsu.cli.commands.ListCmd
import ru.yarsu.cli.commands.ReportCmd
import ru.yarsu.cli.commands.ShowShipmentCmd
import ru.yarsu.csv.CsvParser
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import java.io.File
import kotlin.system.exitProcess

data class ParsedArgs(
    val pathArgs: PathArgs,
    val portArgs: PortArgs,
    val keyArgs: KeyArgs,
    val command: String?,
)

fun main(args: Array<String>) {
    try {
        val parsed = parseArguments(args)
        val pathArgs = parsed.pathArgs
        val portArgs = parsed.portArgs
        val keyArgs = parsed.keyArgs
        val command = parsed.command
        val csvParser = CsvParser()

        if (command == null) {
            startWebServer(pathArgs, portArgs, keyArgs, csvParser)
        } else {
            executeCommand(command, pathArgs, csvParser)
        }
    } catch (e: Exception) {
        println("Ошибка: ${e.message}")
        exitProcess(1)
    }
}

private fun parseArguments(args: Array<String>): ParsedArgs {
    val pathArgs = PathArgs()
    val portArgs = PortArgs()
    val keyArgs = KeyArgs()

    val jCommander =
        JCommander
            .newBuilder()
            .addObject(pathArgs)
            .addObject(portArgs)
            .addObject(keyArgs)
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

    return ParsedArgs(pathArgs, portArgs, keyArgs, jCommander.parsedCommand)
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
    keyArgs: KeyArgs,
    csvParser: CsvParser,
) {
    val (shipmentsFile, trucksFile, employeesFile) = validateAndGetFiles(pathArgs)

    val shipmentList = csvParser.shipmentParser(File(shipmentsFile))
    val trucksList = csvParser.trucksParser(File(trucksFile))
    val employeesList = csvParser.employeesParser(File(employeesFile))

    val shipmentStorage = ShipmentStorage(shipmentList)
    val trucksStorage = TrucksStorage(trucksList)
    val employeesStorage = EmployeesStorage(employeesList, keyArgs.secret.toString())

    val jwtTools = JwtTools(keyArgs.secret.toString())

    val webServer = WebServer()
    webServer.start(
        shipmentStorage,
        trucksStorage,
        employeesStorage,
        portArgs.ports,
        shipmentsFile,
        trucksFile,
        jwtTools,
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
