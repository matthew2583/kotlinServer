package ru.yarsu.web.routes.handlers.get

import org.http4k.core.HttpHandler
import ru.yarsu.domain.Roles
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.web.routes.util.AuthUtils
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils

fun employeesListHandler(employeesStorage: EmployeesStorage): HttpHandler =
    employees@{ request ->

        val (_, errorResponse) = AuthUtils.requireRole(request, Roles.UserManager)
        if (errorResponse != null) return@employees errorResponse

        val allEmployees = employeesStorage.getAllEmployees()
        val sorted = SortingUtils.sortEmployeesByName(allEmployees)

        val responseData =
            sorted.map {
                mapOf(
                    "Id" to it.id.toString(),
                    "Name" to it.name,
                    "Position" to it.position,
                    "RegistrationDateTime" to it.registrationDateTime.toString(),
                    "Email" to it.email,
                )
            }

        GetResponse.responseOK(responseData)
    }
