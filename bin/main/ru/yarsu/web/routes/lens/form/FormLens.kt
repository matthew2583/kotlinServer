package ru.yarsu.web.routes.lens.form

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.string
import org.http4k.lens.webForm
import ru.yarsu.web.routes.dto.form.InvoiceRequest

private val invoiceTitleLens = FormField.string().optional("InvoiceTitle")
private val invoiceTypeLens = FormField.string().optional("InvoiceType")
private val invoiceWeightLens = FormField.string().optional("InvoiceWeight")
private val invoicePriceLens = FormField.string().optional("InvoicePrice")
private val invoiceCostLens = FormField.string().optional("InvoiceCost")
private val dumpTruckModelLens = FormField.string().optional("DumpTruckModel")
private val dumpTruckRegistrationLens = FormField.string().optional("DumpTruckRegistration")
private val managerLens = FormField.string().optional("Manager")

private val rawInvoiceFormBodyLens =
    Body
        .webForm(
            Validator.Ignore,
            invoiceTitleLens,
            invoiceTypeLens,
            invoiceWeightLens,
            invoicePriceLens,
            invoiceCostLens,
            dumpTruckModelLens,
            dumpTruckRegistrationLens,
            managerLens,
        ).toLens()

fun rawInvoiceFormLens(request: Request): InvoiceRequest { // Возвращаем InvoiceRequest
    val form = rawInvoiceFormBodyLens(request)
    return InvoiceRequest(
        invoiceTitle = invoiceTitleLens(form),
        invoiceType = invoiceTypeLens(form),
        invoiceWeight = invoiceWeightLens(form),
        invoicePrice = invoicePriceLens(form),
        invoiceCost = invoiceCostLens(form),
        dumpTruckModel = dumpTruckModelLens(form),
        dumpTruckRegistration = dumpTruckRegistrationLens(form),
        manager = managerLens(form),
    )
}
