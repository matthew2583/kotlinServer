package ru.yarsu.web.routes.dto.json

import ru.yarsu.internal.SwgType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ValidShipmentRequest(
    val title: String,
    val swg: SwgType,
    val measure: String,
    val count: BigDecimal,
    val price: BigDecimal,
    val cost: BigDecimal,
    val shipmentDateTime: LocalDateTime,
    val washing: Boolean,
    val dumpTruckId: UUID,
    val managerId: UUID?,
)

fun ShipmentRequest.asValidRequestOrNull(): ValidShipmentRequest? {
    val title = parseTitle() ?: return null
    val swg = parseSwgType() ?: return null
    val measure = parseMeasure() ?: return null

    val count = parseBigDecimal(count) ?: return null
    val price = parseBigDecimal(price) ?: return null
    val cost = parseBigDecimal(cost) ?: return null

    val dumpTruckId = parseUuid(dumpTruck) ?: return null
    val managerId = parseUuid(manager)

    val shipmentDateTime = parseDateTimeWithDefault()
    val washing = parseBoolean()

    return ValidShipmentRequest(
        title = title,
        swg = swg,
        measure = measure,
        count = count,
        price = price,
        cost = cost,
        shipmentDateTime = shipmentDateTime,
        washing = washing,
        dumpTruckId = dumpTruckId,
        managerId = managerId,
    )
}
