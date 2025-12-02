package ru.yarsu.web.routes.dto.json

fun ShipmentRequest.validateToErrors(): Map<String, Map<String, Any?>> {
    val errors = mutableMapOf<String, Map<String, Any?>>()

    when {
        title == null -> errors["Title"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        title !is String -> errors["Title"] = mapOf("Value" to title, "Error" to "Ожидается строковое значение")
        title.isBlank() -> errors["Title"] = mapOf("Value" to title, "Error" to "Ожидается непустая строка")
        else -> {}
    }

    when {
        swg == null -> errors["SWG"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        swg !is String -> errors["SWG"] = mapOf("Value" to swg, "Error" to "Ожидается строковое значение")
        parseSwgType() == null -> errors["SWG"] = mapOf("Value" to swg, "Error" to "Ожидается корректный тип ПГС")
        else -> {}
    }

    when {
        measure == null -> errors["Measure"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        measure !is String -> errors["Measure"] = mapOf("Value" to measure, "Error" to "Ожидается строковое значение")
        parseMeasure() == null -> errors["Measure"] = mapOf("Value" to measure, "Error" to "Ожидается 'м3' или 'т'")
        else -> {}
    }

    when {
        count == null -> errors["Count"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        count !is String && count !is Number -> errors["Count"] = mapOf("Value" to count, "Error" to "Ожидается числовое значение")
        parseBigDecimal(count) == null -> errors["Count"] = mapOf("Value" to count, "Error" to "Ожидается положительное число")
        else -> {}
    }

    when {
        price == null -> errors["Price"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        price !is String && price !is Number -> errors["Price"] = mapOf("Value" to price, "Error" to "Ожидается числовое значение")
        parseBigDecimal(price) == null -> errors["Price"] = mapOf("Value" to price, "Error" to "Ожидается положительное число")
        else -> {}
    }

    when {
        cost == null -> errors["Cost"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        cost !is String && cost !is Number -> errors["Cost"] = mapOf("Value" to cost, "Error" to "Ожидается числовое значение")
        parseBigDecimal(cost) == null -> errors["Cost"] = mapOf("Value" to cost, "Error" to "Ожидается положительное число")
        else -> {}
    }

    when {
        dumpTruck == null -> errors["DumpTruck"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        dumpTruck !is String -> errors["DumpTruck"] = mapOf("Value" to dumpTruck, "Error" to "Ожидается строковое значение")
        parseUuid(dumpTruck) == null -> errors["DumpTruck"] = mapOf("Value" to dumpTruck, "Error" to "Ожидается корректный UUID")
        else -> {}
    }

    when {
        manager == null -> errors["Manager"] = mapOf("Value" to null, "Error" to "Поле обязательно")
        manager !is String -> errors["Manager"] = mapOf("Value" to manager, "Error" to "Ожидается строковое значение")
        parseUuid(manager) == null -> errors["Manager"] = mapOf("Value" to manager, "Error" to "Ожидается корректный UUID")
        else -> {}
    }

    when {
        shipmentDateTime == null ->
            errors["ShipmentDateTime"] =
                mapOf("Value" to null, "Error" to "Поле должно быть строкой или отсутствовать")
        shipmentDateTime !is String ->
            errors["ShipmentDateTime"] =
                mapOf("Value" to shipmentDateTime, "Error" to "Ожидается строковое значение")
        parseDateTime() == null ->
            errors["ShipmentDateTime"] =
                mapOf("Value" to shipmentDateTime, "Error" to "Ожидается корректная дата и время")
        else -> {}
    }

    when {
        washing == null -> errors["Washing"] = mapOf("Value" to null, "Error" to "Поле должно быть булевым или отсутствовать")
        washing !is String && washing !is Boolean -> errors["Washing"] = mapOf("Value" to washing, "Error" to "Ожидается булево значение")
        washing is String && washing.toBooleanStrictOrNull() == null ->
            errors["Washing"] =
                mapOf("Value" to washing, "Error" to "Ожидается 'true' или 'false'")
        else -> {}
    }

    return errors
}
