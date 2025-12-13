package ru.yarsu.web.routes.dto.json

import com.fasterxml.jackson.annotation.JsonProperty
import ru.yarsu.internal.SwgType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ShipmentRequest(
    @field:JsonProperty("Title")
    val title: Any?,
    @field:JsonProperty("SWG")
    val swg: Any?,
    @field:JsonProperty("Measure")
    val measure: Any?,
    @field:JsonProperty("Count")
    val count: Any?,
    @field:JsonProperty("Price")
    val price: Any?,
    @field:JsonProperty("Cost")
    val cost: Any?,
    @field:JsonProperty("ShipmentDateTime")
    val shipmentDateTime: Any?,
    @field:JsonProperty("Washing")
    val washing: Any?,
    @field:JsonProperty("DumpTruck")
    val dumpTruck: Any?,
    @field:JsonProperty("Manager")
    val manager: Any?,
)

fun ShipmentRequest.parseTitle(): String? =
    when (title) {
        is String -> title.takeIf { it.isNotBlank() }
        else -> null
    }

fun ShipmentRequest.parseSwgType(): SwgType? =
    when (swg) {
        is String -> runCatching { SwgType.fromString(swg.toString().trim()) }.getOrNull()
        else -> null
    }

fun ShipmentRequest.parseMeasure(): String? =
    when (measure) {
        is String -> measure.trim().takeIf { it in setOf("м3", "т") }
        else -> null
    }

fun ShipmentRequest.parseBigDecimal(field: Any?): BigDecimal? =
    when (field) {
        is String ->
            field
                .replace(',', '.')
                .trim()
                .toBigDecimalOrNull()
                ?.takeIf { it > BigDecimal.ZERO }
        is Number -> BigDecimal(field.toString()).takeIf { it > BigDecimal.ZERO }
        else -> null
    }

fun ShipmentRequest.parseDateTime(): LocalDateTime? =
    when (shipmentDateTime) {
        is String -> runCatching { LocalDateTime.parse(shipmentDateTime.toString().trim()) }.getOrNull()
        else -> null
    }

fun ShipmentRequest.parseDateTimeWithDefault(): LocalDateTime = parseDateTime() ?: LocalDateTime.now()

fun ShipmentRequest.parseBoolean(): Boolean =
    when (washing) {
        is String -> washing.toBooleanStrictOrNull() ?: false
        is Boolean -> washing
        else -> false
    }

fun ShipmentRequest.parseUuid(field: Any?): UUID? =
    when (field) {
        is String -> runCatching { UUID.fromString(field.trim()) }.getOrNull()
        else -> null
    }
