package ru.yarsu.application

import ru.yarsu.csv.CsvWrite
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import kotlin.system.exitProcess

fun shutdownHook(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    shipmentsFile: String,
    trucksFile: String,
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
