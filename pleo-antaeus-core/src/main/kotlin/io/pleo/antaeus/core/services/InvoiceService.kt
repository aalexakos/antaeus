/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice

class InvoiceService(private val dal: AntaeusDal, private val paymentProvider: PaymentProvider) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun sent(): List<Invoice> {
        return dal.fetchUnpaidInvoices()
    }

    fun payInvoice(id: Int): Invoice {
        val invoice: Invoice = dal.fetchInvoice(id)!!
        if (!invoice.let { paymentProvider.charge(it) }) {
            throw InvoiceNotFoundException(invoice.id)
        }
        dal.setPaidStatus(invoice.id)
        return dal.fetchInvoice(id)!!
    }
}
