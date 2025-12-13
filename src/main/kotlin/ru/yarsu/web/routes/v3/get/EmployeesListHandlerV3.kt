package ru.yarsu.web.routes.v3.get

import org.http4k.core.HttpHandler
import ru.yarsu.internal.Roles
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.web.routes.filter.AuthKeys
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils

fun employeesListHandlerV3(employeesStorage: EmployeesStorage): HttpHandler =
    employees@{ request ->

        val role =
            AuthKeys.employeeRoleKey(request)
                ?: return@employees GetResponse.responseUnauthorized(
                    mapOf("Error" to "Отказанно в авторизации"),
                )

        if (role != Roles.UserManager) {
            return@employees GetResponse.responseUnauthorized(
                mapOf("Error" to "Отказанно в авторизации"),
            )
        }

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
