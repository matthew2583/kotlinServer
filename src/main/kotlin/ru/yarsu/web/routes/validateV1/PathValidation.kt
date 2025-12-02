package ru.yarsu.web.routes.validateV1

import org.http4k.core.Response
import ru.yarsu.web.routes.util.GetResponse
import java.util.UUID

object PathValidation {
    private fun error(message: String): Response = GetResponse.responseBadRequest(message)

    fun validateId(id: String?): Pair<Response?, UUID?> {
        if (id.isNullOrBlank()) {
            return error(
                "Некорректное значение параметра ID: параметр отсутствует",
            ) to null
        }

        return try {
            null to UUID.fromString(id.trim())
        } catch (e: IllegalArgumentException) {
            error(
                "Некорректное значение параметра ID: " +
                    "значение должно быть в формате UUID, но получено: $id",
            ) to null
        }
    }
}
