package ru.yarsu.web.routes.dto.form

import ru.yarsu.domain.SwgType
import java.util.UUID

data class InvoiceRequestValid(
    val invoiceTitle: String,
    val invoiceType: SwgType,
    val invoiceWeight: Double,
    val invoicePrice: Double,
    val invoiceCost: Double,
    val dumpTruckModel: String,
    val dumpTruckRegistration: String,
    val manager: UUID,
)

fun InvoiceRequest.asValidFormOrNull(): InvoiceRequestValid? {
    val title = parseTitle() ?: return null
    val swgType = parseSwgType() ?: return null
    val weight = parseWeight() ?: return null
    val price = parsePrice() ?: return null

    val cost = parseCost() ?: calculateDefaultCost(weight, price)

    val model = parseDumpTruckModel() ?: return null
    val registration = parseDumpTruckRegistration() ?: return null
    val managerUuid = parseManager() ?: return null

    return InvoiceRequestValid(
        invoiceTitle = title,
        invoiceType = swgType,
        invoiceWeight = weight,
        invoicePrice = price,
        invoiceCost = cost,
        dumpTruckModel = model,
        dumpTruckRegistration = registration,
        manager = managerUuid,
    )
}
