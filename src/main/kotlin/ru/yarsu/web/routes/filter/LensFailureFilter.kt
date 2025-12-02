package ru.yarsu.web.routes.filter

import org.http4k.core.Filter
import org.http4k.lens.LensFailure
import ru.yarsu.web.routes.util.GetResponse
import kotlin.text.isNotBlank
import kotlin.to

val lensFailureFilter =
    Filter { next ->
        { request ->
            try {
                next(request)
            } catch (e: LensFailure) {
                val body = request.bodyString()
                val errorMessage = e.message ?: "Ошибка при обработке данных запроса"

                if (e.cause != null && body.isNotBlank()) {
                    GetResponse.responseBadRequest(
                        mapOf(
                            "Value" to body,
                            "Error" to errorMessage,
                        ),
                    )
                } else {
                    GetResponse.responseBadRequest(
                        mapOf(
                            "Error" to errorMessage,
                        ),
                    )
                }
            }
        }
    }
