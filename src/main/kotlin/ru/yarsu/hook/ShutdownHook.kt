package ru.yarsu.hook

import ru.yarsu.internal.CsvWrite
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import kotlin.system.exitProcess

fun shutdownHook(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
    shipmentsFile: String,
    trucksFile: String,
    employeesFile: String,
) {
    Runtime.getRuntime().addShutdownHook(
        Thread {
            try {
                val shipments = shipmentStorage.getAllShipments()
                val trucks = trucksStorage.getAllTrucks()

                CsvWrite().writeShipments(shipments, shipmentsFile)
                CsvWrite().writeTrucks(trucks, trucksFile)

                println("Сохранение всех данных выполнено!")
            } catch (e: Exception) {
                println("Ошибка сохранения: ${e.message}")
                exitProcess(1)
            }
        },
    )
}
