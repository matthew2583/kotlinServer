package ru.yarsu.application

import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.application.shutdownHook
import ru.yarsu.auth.JwtTools
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.applicationRoutes
import ru.yarsu.web.routes.filter.lensFailureFilter

class WebServer {
    fun start(
        shipmentStorage: ShipmentStorage,
        trucksStorage: TrucksStorage,
        employeesStorage: EmployeesStorage,
        port: Int,
        shipmentsFile: String,
        trucksFile: String,
        jwtTools: JwtTools,
    ) {
        shutdownHook(
            shipmentStorage,
            trucksStorage,
            shipmentsFile,
            trucksFile,
        )

        val routes =
            applicationRoutes(
                shipmentStorage,
                trucksStorage,
                employeesStorage,
                jwtTools,
            ).withFilter(lensFailureFilter)

        routes.asServer(Netty(port)).start()
        println("Сервер запущен: http://localhost:$port")
    }
}
