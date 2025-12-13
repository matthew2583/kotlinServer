package ru.yarsu.web.routes.dto.json

fun ShipmentRequest.validateToErrors(): Map<String, Map<String, Any?>> {
    val errors: Errors = mutableMapOf()

    requireNonBlankString(errors, "Title", title)

    requireParsedString(
        errors = errors,
        field = "SWG",
        value = swg,
        parseFn = { _ -> parseSwgType() },
        invalidMessage = "Ожидается корректный тип ПГС",
    )

    requireParsedString(
        errors = errors,
        field = "Measure",
        value = measure,
        parseFn = { _ -> parseMeasure() },
        invalidMessage = "Ожидается 'м3' или 'т'",
    )

    requireParsedNumber(errors, "Count", count, parseFn = { parseBigDecimal(it) })
    requireParsedNumber(errors, "Price", price, parseFn = { parseBigDecimal(it) })
    requireParsedNumber(errors, "Cost", cost, parseFn = { parseBigDecimal(it) })

    requireParsedUuidString(errors, "DumpTruck", dumpTruck, parseFn = { parseUuid(it) })

    if (manager != null) {
        requireParsedUuidString(
            errors = errors,
            field = "Manager",
            value = manager,
            parseFn = { parseUuid(it) },
            requiredMessage = "Поле обязательно",
            typeMessage = "Ожидается строковое значение",
            invalidMessage = "Ожидается корректный UUID",
        )
    }

    if (shipmentDateTime != null) {
        validateDateTimeLikeYourCurrent(
            errors = errors,
            field = "ShipmentDateTime",
            value = shipmentDateTime,
            parseFn = { parseDateTime() },
        )
    }

    if (washing != null) {
        validateBooleanLikeYourCurrent(
            errors = errors,
            field = "Washing",
            value = washing,
        )
    }

    return errors
}
