package ru.yarsu.web.routes.filter

import org.http4k.lens.RequestKey
import ru.yarsu.internal.Roles
import java.util.UUID

object AuthKeys {
    val employeeIdKey = RequestKey.optional<UUID>("employeeId")
    val employeeRoleKey = RequestKey.optional<Roles>("employeeRole")
    val employeeEmailKey = RequestKey.optional<String>("employeeEmail")
}
