package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val dal: AntaeusDal
) {
    // TODO - Add code e.g. here
    fun payPendingInvoices(id: Int): Invoice {
        dal.setPaidStatus(id)
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }
}
