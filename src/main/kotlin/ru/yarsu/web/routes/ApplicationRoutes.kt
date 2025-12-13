package ru.yarsu.web.routes

import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.yarsu.jwt.JwtTools
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.storage.ShipmentStorage
import ru.yarsu.storage.TrucksStorage
import ru.yarsu.web.routes.filter.AuthorizationFilter
import ru.yarsu.web.routes.v1.bySwgTypeListPagedHandler
import ru.yarsu.web.routes.v1.dumpTrucksIDListPagedHandler
import ru.yarsu.web.routes.v1.reportByMonthHandler
import ru.yarsu.web.routes.v1.shipmentByPeriodListHandler
import ru.yarsu.web.routes.v1.shipmentsIDListHandler
import ru.yarsu.web.routes.v1.shipmentsListPagedHandler
import ru.yarsu.web.routes.v2.del.deleteTrucksHandlerV2
import ru.yarsu.web.routes.v2.get.bySwgTypeListPagedHandlerV2
import ru.yarsu.web.routes.v2.get.dumpTrucksIDListPagedHandlerV2
import ru.yarsu.web.routes.v2.get.employeesListHandlerV2
import ru.yarsu.web.routes.v2.get.reportByMonthHandlerV2
import ru.yarsu.web.routes.v2.get.shipmentByPeriodListHandlerV2
import ru.yarsu.web.routes.v2.get.shipmentsIDListHandlerV2
import ru.yarsu.web.routes.v2.get.shipmentsListPagedHandlerV2
import ru.yarsu.web.routes.v2.post.createShipmentHandlerV2
import ru.yarsu.web.routes.v2.post.invoiceRegistrationHandlerV2
import ru.yarsu.web.routes.v2.put.updateShipmentHandlerV2
import ru.yarsu.web.routes.v3.authorization.tokensListHandlerV3
import ru.yarsu.web.routes.v3.del.deleteTrucksHandlerV3
import ru.yarsu.web.routes.v3.get.bySwgTypeListPagedHandlerV3
import ru.yarsu.web.routes.v3.get.dumpTrucksIDListPagedHandlerV3
import ru.yarsu.web.routes.v3.get.employeesListHandlerV3
import ru.yarsu.web.routes.v3.get.reportByMonthHandlerV3
import ru.yarsu.web.routes.v3.get.shipmentByPeriodListHandlerV3
import ru.yarsu.web.routes.v3.get.shipmentsIDListHandlerV3
import ru.yarsu.web.routes.v3.get.shipmentsListPagedHandlerV3
import ru.yarsu.web.routes.v3.post.createShipmentHandlerV3
import ru.yarsu.web.routes.v3.post.invoiceRegistrationHandlerV3
import ru.yarsu.web.routes.v3.put.updateShipmentHandlerV3

fun applicationRoutes(
    shipmentStorage: ShipmentStorage,
    trucksStorage: TrucksStorage,
    employeesStorage: EmployeesStorage,
    jwtTools: JwtTools,
): RoutingHttpHandler {
    val authFilter = AuthorizationFilter(jwtTools, employeesStorage)

    return routes(
        // base route
        "/" bind GET to rootHandler,
        "/ping" bind GET to pingHandler,
        // v1 route
        "v1/shipments" bind GET to shipmentsListPagedHandler(shipmentStorage),
        "v1/shipments/by-type" bind GET to bySwgTypeListPagedHandler(shipmentStorage),
        "v1/shipments/report" bind GET to reportByMonthHandler(shipmentStorage),
        "v1/shipments/by-period" bind GET to shipmentByPeriodListHandler(shipmentStorage),
        "v1/shipments/{shipment-id}" bind GET to shipmentsIDListHandler(shipmentStorage, trucksStorage),
        "v1/dump-trucks/{dump-truck-id}" bind GET to dumpTrucksIDListPagedHandler(shipmentStorage, trucksStorage),
        // v2 route
        "v2/shipments" bind GET to shipmentsListPagedHandlerV2(shipmentStorage),
        "v2/shipments" bind POST to createShipmentHandlerV2(shipmentStorage, trucksStorage, employeesStorage),
        "v2/shipments/by-type" bind GET to bySwgTypeListPagedHandlerV2(shipmentStorage),
        "v2/shipments/by-period" bind GET to shipmentByPeriodListHandlerV2(shipmentStorage),
        "v2/shipments/report" bind GET to reportByMonthHandlerV2(shipmentStorage),
        "v2/shipments/{shipment-id}" bind GET to
            shipmentsIDListHandlerV2(
                shipmentStorage,
                trucksStorage,
                employeesStorage,
            ),
        "v2/shipments/{shipment-id}" bind PUT to
            updateShipmentHandlerV2(
                shipmentStorage,
                trucksStorage,
                employeesStorage,
            ),
        "v2/dump-trucks/{dump-truck-id}" bind GET to dumpTrucksIDListPagedHandlerV2(shipmentStorage, trucksStorage),
        "v2/dump-trucks/{dump-truck-id}" bind DELETE to deleteTrucksHandlerV2(shipmentStorage, trucksStorage),
        "v2/register-invoice" bind POST to
            invoiceRegistrationHandlerV2(
                shipmentStorage,
                trucksStorage,
                employeesStorage,
            ),
        "v2/employees" bind GET to employeesListHandlerV2(employeesStorage),
        // v3
        "v3/tokens" bind GET to tokensListHandlerV3(employeesStorage),
        "v3/shipments" bind GET to shipmentsListPagedHandlerV3(shipmentStorage),
        "v3/shipments" bind POST to
            authFilter.then(createShipmentHandlerV3(shipmentStorage, trucksStorage, employeesStorage)),
        "v3/shipments/by-type" bind GET to authFilter.then(bySwgTypeListPagedHandlerV3(shipmentStorage)),
        "v2/shipments/by-period" bind GET to authFilter.then(shipmentByPeriodListHandlerV3(shipmentStorage)),
        "v3/shipments/report" bind GET to authFilter.then(reportByMonthHandlerV3(shipmentStorage)),
        "v3/shipments/{shipment-id}" bind GET to
            authFilter.then(
                shipmentsIDListHandlerV3(
                    shipmentStorage,
                    trucksStorage,
                    employeesStorage,
                ),
            ),
        "v3/shipments/{shipment-id}" bind PUT to
            authFilter.then(
                updateShipmentHandlerV3(
                    shipmentStorage,
                    trucksStorage,
                    employeesStorage,
                ),
            ),
        "v3/register-invoice" bind POST to
            authFilter.then(
                invoiceRegistrationHandlerV3(
                    shipmentStorage,
                    trucksStorage,
                ),
            ),
        "v3/dump-trucks/{dump-truck-id}" bind GET to
            authFilter.then(dumpTrucksIDListPagedHandlerV3(shipmentStorage, trucksStorage)),
        "v3/dump-trucks/{dump-truck-id}" bind DELETE to
            authFilter.then(deleteTrucksHandlerV3(shipmentStorage, trucksStorage)),
        "v3/employees" bind GET to authFilter.then(employeesListHandlerV3(employeesStorage)),
    )
}

private val rootHandler: (org.http4k.core.Request) -> Response = {
    Response(Status.OK)
        .body("Приложение для управления продажей песчано-гравийной смеси")
        .header("Content-Type", "text/plain; charset=utf-8")
}

private val pingHandler: (org.http4k.core.Request) -> Response = {
    Response(Status.OK)
}

// http://localhost:9000/v2/shipments?page=1
// http://localhost:9000/v2/shipments
// http://localhost:9000/v2/shipments/by-type?by-swg-type=Песок речной&page=1
// http://localhost:9000/v2/shipments/by-period?from=2024-01-01&to=2024-02-01&page=1
// http://localhost:9000/v2/shipments/report?year=2024
// http://localhost:9000/v2/shipments/2b52fcb1-34ec-48d2-95f2-b79f2d9c3c70
// http://localhost:9000/v2/shipments/2b52fcb1-34ec-48d2-95f2-b79f2d9c3c70
// http://localhost:9000/v2/dump-trucks/f8741785-4986-41ab-85d3-8b2fe522f9ed
// http://localhost:9000/v2/dump-trucks/f8741785-4986-41ab-85d3-8b2fe522f9ed
// http://localhost:9000/v2/register-invoice
// http://localhost:9000/v2/employees
