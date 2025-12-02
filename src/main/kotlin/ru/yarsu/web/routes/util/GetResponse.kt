package ru.yarsu.web.routes.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.Response
import org.http4k.core.Status

object GetResponse {
    private val mapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

    // 200
    fun responseOK(body: Any): Response =
        Response(Status.OK)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))

    // 400
    fun responseBadRequest(errorMessage: Any): Response =
        Response(Status.BAD_REQUEST)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(errorMessage))

    // 404
    fun responseNotFound(body: Any): Response =
        Response(Status.NOT_FOUND)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))

    // 201
    fun responseCreated(body: Any): Response =
        Response(Status.CREATED)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))

    // 403
    fun responseForbidden(body: Any): Response =
        Response(Status.FORBIDDEN)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))

    // 409
    fun responseConflict(body: Any): Response =
        Response(Status.CONFLICT)
            .header("Content-Type", "application/json; charset=utf-8")
            .body(mapper.writeValueAsString(body))

    // 204
    fun responseNoContent(): Response = Response(Status.NO_CONTENT)
}
