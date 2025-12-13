package ru.yarsu.web.routes.v3.authorization

import org.http4k.core.HttpHandler
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.web.routes.util.GetResponse

fun tokensListHandlerV3(employeesStorage: EmployeesStorage): HttpHandler =
    { _ ->
        val tokens = employeesStorage.getTokens()

        val responseData =
            tokens.map { token ->
                mapOf("token" to token)
            }

        GetResponse.responseOK(responseData)
    }
