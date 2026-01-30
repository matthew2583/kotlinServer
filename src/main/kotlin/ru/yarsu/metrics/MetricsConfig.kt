package ru.yarsu.metrics

import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.MicrometerMetrics
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

object MetricsConfig {
    val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    /**
     * Фильтр для сбора метрик HTTP-запросов
     * Собирает: количество запросов, время ответа, статус-коды
     */
    fun metricsFilter() = ServerFilters.MicrometerMetrics.RequestCounter(prometheusRegistry)
        .then(ServerFilters.MicrometerMetrics.RequestTimer(prometheusRegistry))

    /**
     * Эндпоинт /metrics для Prometheus scraping
     */
    fun metricsEndpoint(): RoutingHttpHandler = routes(
        "/metrics" bind Method.GET to {
            Response(Status.OK)
                .header("Content-Type", "text/plain; version=0.0.4")
                .body(prometheusRegistry.scrape())
        }
    )

    /**
     * Регистрация кастомных метрик приложения
     */
    fun registerCustomMetrics(
        shipmentCount: () -> Int,
        trucksCount: () -> Int,
        employeesCount: () -> Int,
    ) {
        prometheusRegistry.gauge("app_shipments_total", Unit) { shipmentCount().toDouble() }
        prometheusRegistry.gauge("app_trucks_total", Unit) { trucksCount().toDouble() }
        prometheusRegistry.gauge("app_employees_total", Unit) { employeesCount().toDouble() }
    }
}
