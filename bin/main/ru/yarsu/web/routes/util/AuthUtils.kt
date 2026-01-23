package ru.yarsu.web.routes.util

import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.domain.Roles
import ru.yarsu.web.routes.filter.AuthKeys
import java.util.UUID

sealed class AuthResult {
    data class Success(
        val id: UUID,
        val role: Roles,
        val email: String,
    ) : AuthResult()

    data class Failure(
        val response: Response,
    ) : AuthResult()
}

object AuthUtils {
    fun requireRoles(
        request: Request,
        vararg allowedRoles: Roles,
    ): AuthResult {
        val id =
            AuthKeys.employeeIdKey(request)
                ?: return AuthResult.Failure(
                    GetResponse.responseUnauthorized(mapOf("Error" to ErrorMessages.UNAUTHORIZED)),
                )

        val role =
            AuthKeys.employeeRoleKey(request)
                ?: return AuthResult.Failure(
                    GetResponse.responseUnauthorized(mapOf("Error" to ErrorMessages.UNAUTHORIZED)),
                )

        val email = AuthKeys.employeeEmailKey(request) ?: ""

        if (allowedRoles.isNotEmpty() && role !in allowedRoles) {
            return AuthResult.Failure(GetResponse.responseForbidden(mapOf("Error" to ErrorMessages.FORBIDDEN)))
        }

        return AuthResult.Success(id, role, email)
    }

    fun requireRole(
        request: Request,
        vararg allowedRoles: Roles,
    ): Pair<Roles?, Response?> {
        val role =
            AuthKeys.employeeRoleKey(request)
                ?: return null to GetResponse.responseUnauthorized(mapOf("Error" to ErrorMessages.UNAUTHORIZED))

        if (allowedRoles.isNotEmpty() && role !in allowedRoles) {
            return null to GetResponse.responseUnauthorized(mapOf("Error" to ErrorMessages.UNAUTHORIZED))
        }

        return role to null
    }
}
