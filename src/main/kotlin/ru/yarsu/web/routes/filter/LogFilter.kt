package ru.yarsu.web.routes.filter

import org.http4k.core.Filter

val logFilter =
    Filter { next ->
        { request ->
            println(request.bodyString())
            next(request)
        }
    }
