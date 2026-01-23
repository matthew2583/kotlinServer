package ru.yarsu.web.routes.filter

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import ru.yarsu.auth.JwtTools
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.web.routes.handlers.authorization.AuthorizationFilterHandler

class AuthorizationFilter(
    private val jwtTools: JwtTools,
    private val employeesStorage: EmployeesStorage,
) : Filter {
    override fun invoke(handler: HttpHandler): HttpHandler =
        AuthorizationFilterHandler(
            jwtTools = jwtTools,
            handler = handler,
            employeesStorage = employeesStorage,
        )
}
