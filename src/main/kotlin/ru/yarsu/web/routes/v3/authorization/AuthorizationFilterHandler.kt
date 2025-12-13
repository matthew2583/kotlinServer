package ru.yarsu.web.routes.v3.authorization

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.jwt.JwtTools
import ru.yarsu.storage.EmployeesStorage
import ru.yarsu.web.routes.filter.AuthKeys
import ru.yarsu.web.routes.util.GetResponse
import java.util.UUID

class AuthorizationFilterHandler(
    private val jwtTools: JwtTools,
    private val handler: HttpHandler,
    private val employeesStorage: EmployeesStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val header =
            request.header("Authorization")
                ?: return unauthorized("Отсутствует заголовок Authorization")

        if (!header.startsWith("Bearer ")) {
            return unauthorized("Некорректная схема авторизации (ожидается Bearer)")
        }

        val token = header.removePrefix("Bearer").trim()
        if (token.isBlank()) return unauthorized("Пустой токен")

        val decoded =
            jwtTools.verification(token)
                ?: return unauthorized("Недействительный или истёкший токен")

        if (decoded.expiresAt == null) {
            return unauthorized("Отсутствует expiresAt")
        }

        val subject = decoded.subject
        if (subject.isNullOrBlank()) return unauthorized("В токене отсутствует subject")

        val employeeId =
            runCatching { UUID.fromString(subject) }.getOrNull()
                ?: return unauthorized("Некорректный subject: ожидается UUID")

        val employee =
            employeesStorage.getEmployeesById(employeeId)
                ?: return unauthorized("Работник по токену не найден")

        val req1 = AuthKeys.employeeIdKey(employee.id, request)
        val req2 = AuthKeys.employeeRoleKey(employee.role, req1)
        val req3 = AuthKeys.employeeEmailKey(employee.email, req2)

        return handler(req3)
    }

    private fun unauthorized(message: String): Response = GetResponse.responseUnauthorized(mapOf("Error" to message))
}
