package ru.yarsu.data

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yarsu.domain.SwgType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Shipment(
    @field:JsonProperty("Id")
    val id: UUID,
    @field:JsonProperty("Title")
    val title: String,
    @field:JsonProperty("Swg")
    val swg: SwgType,
    @field:JsonProperty("Measure")
    val measure: String,
    @field:JsonProperty("Count")
    val count: BigDecimal,
    @field:JsonProperty("Price")
    val price: BigDecimal,
    @field:JsonProperty("Cost")
    val cost: BigDecimal,
    @field:JsonProperty("ShipmentDateTime")
    val shipmentDateTime: LocalDateTime,
    @field:JsonProperty("DumpTruck")
    val dumpTruck: UUID,
    @field:JsonProperty("Washing")
    val washing: Boolean,
    @field:JsonProperty("Manager")
    val manager: UUID,
)
