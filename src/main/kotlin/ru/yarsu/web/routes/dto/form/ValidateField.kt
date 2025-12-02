package ru.yarsu.web.routes.dto.form

import ru.yarsu.internal.SwgType

fun InvoiceRequest.validateToErrors(): Map<String, Map<String, Any?>> {
    val errors = mutableMapOf<String, Map<String, Any?>>()

    if (parseTitle() == null) {
        errors["InvoiceTitle"] = mapOf("Value" to invoiceTitle, "Error" to "Ожидается непустая строка")
    }

    if (parseSwgType() == null) {
        errors["InvoiceType"] =
            mapOf(
                "Value" to invoiceType,
                "Error" to "Ожидается корректный тип ПГС: " +
                    SwgType.entries.joinToString { it.displayName },
            )
    }

    if (parseWeight() == null) {
        errors["InvoiceWeight"] = mapOf("Value" to invoiceWeight, "Error" to "Ожидается положительное число")
    }

    if (parsePrice() == null) {
        errors["InvoicePrice"] = mapOf("Value" to invoicePrice, "Error" to "Ожидается положительное число")
    }

    if (invoiceCost != null && parseCost() == null) {
        errors["InvoiceCost"] = mapOf("Value" to invoiceCost, "Error" to "Ожидается положительное число")
    }

    if (parseDumpTruckModel() == null) {
        errors["DumpTruckModel"] = mapOf("Value" to dumpTruckModel, "Error" to "Ожидается непустая строка")
    }

    if (parseDumpTruckRegistration() == null) {
        errors["DumpTruckRegistration"] = mapOf("Value" to dumpTruckRegistration, "Error" to "Ожидается непустая строка")
    }

    if (parseManager() == null) {
        errors["Manager"] = mapOf("Value" to manager, "Error" to "Ожидается корректный UUID")
    }

    return errors
}
