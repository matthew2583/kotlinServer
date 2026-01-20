package ru.yarsu.data

import com.fasterxml.jackson.annotation.JsonProperty
import UUID

data class Trucks(
    @field:JsonProperty("Id")
    val id: UUID,
    @field:JsonProperty("Model")
    val model: String,
    @field:JsonProperty("Registration")
    val registration: String,
    @field:JsonProperty("Capacity")
    val capacity: Double,
    @field:JsonProperty("Volume")
    val volume: Double,
)
