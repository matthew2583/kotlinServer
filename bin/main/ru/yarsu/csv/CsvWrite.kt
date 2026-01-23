package ru.yarsu.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import ru.yarsu.data.Shipment
import ru.yarsu.data.Trucks
import java.io.File
import kotlin.system.exitProcess

class CsvWrite {
    fun writeShipments(
        shipments: List<Shipment>,
        path: String,
    ) {
        try {
            csvWriter().open(File(path)) {
                writeRow(
                    "Id",
                    "Title",
                    "SWG",
                    "Measure",
                    "Count",
                    "Price",
                    "Cost",
                    "ShipmentDateTime",
                    "Washing",
                    "DumpTruck",
                    "Manager",
                )
                for (shipment in shipments) {
                    writeRow(
                        shipment.id.toString(),
                        shipment.title,
                        shipment.swg.displayName,
                        shipment.measure,
                        shipment.count.toString(),
                        shipment.price.toString(),
                        shipment.cost.toString(),
                        shipment.shipmentDateTime.toString(),
                        shipment.washing.toString(),
                        shipment.dumpTruck.toString(),
                        shipment.manager.toString(),
                    )
                }
            }
            println("CSV файл отгрузок успешно записан: $path")
        } catch (e: Exception) {
            println("Ошибка при записи файла отгрузок: ${e.message}")
            exitProcess(1)
        }
    }

    fun writeTrucks(
        trucks: List<Trucks>,
        path: String,
    ) {
        try {
            csvWriter().open(File(path)) {
                writeRow(
                    "Id",
                    "Model",
                    "Registration",
                    "Capacity",
                    "Volume",
                )
                for (truck in trucks) {
                    writeRow(
                        truck.id.toString(),
                        truck.model,
                        truck.registration,
                        truck.capacity,
                        truck.volume,
                    )
                }
            }
            println("CSV файл cамосвалов успешно записан: $path")
        } catch (e: Exception) {
            println("Ошибка при записи файла самосвалов: ${e.message}")
            exitProcess(1)
        }
    }
}
