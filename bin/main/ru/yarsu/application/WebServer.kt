package ru.yarsu.application

import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.application.shutdownHook
import ru.yarsu.auth.JwtTools
import ru.yarsu.metrics.MetricsConfig
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

        // Регистрация кастомных метрик
        MetricsConfig.registerCustomMetrics(
            shipmentCount = { shipmentStorage.getAllShipments().size },
            trucksCount = { trucksStorage.getAllTrucks().size },
            employeesCount = { employeesStorage.getAllEmployees().size },
        )

        val appRoutes =
            applicationRoutes(
                shipmentStorage,
                trucksStorage,
                employeesStorage,
                jwtTools,
            ).withFilter(lensFailureFilter)

        // Объединяем роуты приложения с эндпоинтом метрик
        val routesWithMetrics = routes(
            MetricsConfig.metricsEndpoint(),
            appRoutes,
        )

        // Применяем фильтр метрик ко всем запросам
        val app = MetricsConfig.metricsFilter().then(routesWithMetrics)

        val server = app.asServer(Netty(port))
        server.start()
        println("Сервер запущен: http://localhost:$port")
        println("Метрики доступны: http://localhost:$port/metrics")
    }
}
