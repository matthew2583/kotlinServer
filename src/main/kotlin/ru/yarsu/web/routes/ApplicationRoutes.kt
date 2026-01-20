package ru.yarsu.web.routes

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.auth.JwtTools
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.filter.AuthorizationFilter
import ru.yarsu.web.routes.handlers.authorization.tokensListHandler
import ru.yarsu.web.routes.handlers.del.deleteTrucksHandler
import ru.yarsu.web.routes.handlers.get.bySwgTypeListPagedHandler
import ru.yarsu.web.routes.handlers.get.dumpTrucksIDListPagedHandler
import ru.yarsu.web.routes.handlers.get.employeesListHandler
import ru.yarsu.web.routes.handlers.get.reportByMonthHandler
import ru.yarsu.web.routes.handlers.get.shipmentByPeriodListHandler
import ru.yarsu.web.routes.handlers.get.shipmentsIDListHandler
import ru.yarsu.web.routes.handlers.get.shipmentsListPagedHandler
import ru.yarsu.web.routes.handlers.post.createShipmentHandler
import ru.yarsu.web.routes.handlers.post.invoiceRegistrationHandler
import ru.yarsu.web.routes.handlers.put.updateShipmentHandler

fun applicationRoutes(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
    jwtTools: JwtTools,
): RoutingHttpHandler {
    val authFilter = AuthorizationFilter(jwtTools, employeesStorage)

    return routes(
        "/" bind GET to rootHandler,
        "/ping" bind GET to pingHandler,
        // Authentication
        "/api/tokens" bind GET to tokensListHandler(employeesStorage),
        // Shipments
        "/api/shipments" bind GET to shipmentsListPagedHandler(shipmentStorage),
        "/api/shipments" bind POST to
            authFilter.then(createShipmentHandler(shipmentStorage, trucksStorage, employeesStorage)),
        "/api/shipments/by-type" bind GET to authFilter.then(bySwgTypeListPagedHandler(shipmentStorage)),
        "/api/shipments/by-period" bind GET to authFilter.then(shipmentByPeriodListHandler(shipmentStorage)),
        "/api/shipments/report" bind GET to authFilter.then(reportByMonthHandler(shipmentStorage)),
        "/api/shipments/{shipment-id}" bind GET to
            authFilter.then(
                shipmentsIDListHandler(
                    shipmentStorage,
                    trucksStorage,
                    employeesStorage,
                ),
            ),
        "/api/shipments/{shipment-id}" bind PUT to
            authFilter.then(
                updateShipmentHandler(
                    shipmentStorage,
                    trucksStorage,
                    employeesStorage,
                ),
            ),
        // Invoice Registration
        "/api/register-invoice" bind POST to
            authFilter.then(
                invoiceRegistrationHandler(
                    shipmentStorage,
                    trucksStorage,
                ),
            ),
        // Dump Trucks
        "/api/dump-trucks/{dump-truck-id}" bind GET to
            authFilter.then(dumpTrucksIDListPagedHandler(shipmentStorage, trucksStorage)),
        "/api/dump-trucks/{dump-truck-id}" bind DELETE to
            authFilter.then(deleteTrucksHandler(shipmentStorage, trucksStorage)),
        // Employees
        "/api/employees" bind GET to authFilter.then(employeesListHandler(employeesStorage)),
    )
}

private val rootHandler: (Request) -> Response = {
    Response(Status.OK)
        .header("Content-Type", "text/plain; charset=utf-8")
        .body("Sand & Gravel Mix Shipment Management API")
}

private val pingHandler: (Request) -> Response = {
    Response(Status.OK)
}
