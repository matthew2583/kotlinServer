package ru.yarsu.web.routes.dto.form

import ru.yarsu.domain.SwgType
import java.util.UUID

data class InvoiceRequest(
    val invoiceTitle: String?,
    val invoiceType: String?,
    val invoiceWeight: String?,
    val invoicePrice: String?,
    val invoiceCost: String?,
    val dumpTruckModel: String?,
    val dumpTruckRegistration: String?,
    val manager: String?,
)

fun InvoiceRequest.parseTitle(): String? = invoiceTitle?.takeIf { it.isNotBlank() }

fun InvoiceRequest.parseSwgType(): SwgType? =
    invoiceType?.trim()?.let {
        runCatching { SwgType.fromString(it) }.getOrNull()
    }

fun InvoiceRequest.parseWeight(): Double? =
    invoiceWeight
        ?.replace(',', '.')
        ?.trim()
        ?.toDoubleOrNull()
        ?.takeIf { it > 0 }

fun InvoiceRequest.parsePrice(): Double? =
    invoicePrice
        ?.replace(',', '.')
        ?.trim()
        ?.toDoubleOrNull()
        ?.takeIf { it > 0 }

fun InvoiceRequest.parseCost(): Double? =
    invoiceCost
        ?.replace(',', '.')
        ?.trim()
        ?.toDoubleOrNull()
        ?.takeIf { it > 0 }

fun InvoiceRequest.calculateDefaultCost(
    weight: Double,
    price: Double,
): Double = weight * price

fun InvoiceRequest.parseDumpTruckModel(): String? = dumpTruckModel?.takeIf { it.isNotBlank() }

fun InvoiceRequest.parseDumpTruckRegistration(): String? = dumpTruckRegistration?.takeIf { it.isNotBlank() }

fun InvoiceRequest.parseManager(): UUID? =
    manager?.trim()?.let {
        runCatching { UUID.fromString(it) }.getOrNull()
    }
