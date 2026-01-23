package ru.yarsu.data

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yarsu.domain.Roles
import java.time.LocalDateTime
import java.util.UUID

data class Employees(
    @field:JsonProperty("Id")
    val id: UUID,
    @field:JsonProperty("Name")
    val name: String,
    @field:JsonProperty("Position")
    val position: String,
    @field:JsonProperty("RegistrationDateTime")
    val registrationDateTime: LocalDateTime,
    @field:JsonProperty("Email")
    val email: String,
    @field:JsonProperty("Role")
    val role: Roles,
)
