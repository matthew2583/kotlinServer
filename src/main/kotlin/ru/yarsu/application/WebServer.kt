package ru.yarsu.application

import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.hook.shutdownHook
import ru.yarsu.jwt.JwtTools
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
        employeesFile: String,
        jwtTools: JwtTools,
    ) {
        shutdownHook(
            shipmentStorage,
            trucksStorage,
            employeesStorage,
            shipmentsFile,
            trucksFile,
            employeesFile,
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
