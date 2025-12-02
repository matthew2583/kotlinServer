package ru.yarsu.web.routes.v2.get

import org.http4k.core.HttpHandler
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.web.routes.util.GetResponse
import ru.yarsu.web.routes.util.SortingUtils

fun employeesListHandlerV2(employeesStorage: EmployeesStorage): HttpHandler =
    { request ->
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
