package ru.yarsu.web.routes.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.Response
import org.http4k.core.Status

object GetResponse {
    private val mapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

    private fun jsonResponse(
        status: Status,
        body: Any,
    ): Response =
        Response(status)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))

    fun responseOK(body: Any): Response = jsonResponse(Status.OK, body)

    fun responseBadRequest(body: Any): Response = jsonResponse(Status.BAD_REQUEST, body)

    fun responseNotFound(body: Any): Response = jsonResponse(Status.NOT_FOUND, body)

    fun responseCreated(body: Any): Response = jsonResponse(Status.CREATED, body)

    fun responseForbidden(body: Any): Response = jsonResponse(Status.FORBIDDEN, body)

    fun responseConflict(body: Any): Response = jsonResponse(Status.CONFLICT, body)

    fun responseUnauthorized(body: Any): Response = jsonResponse(Status.UNAUTHORIZED, body)

    fun responseNoContent(): Response = Response(Status.NO_CONTENT)
}
