package io.pleo.antaeus.core.services

import io.mockk.MockK
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.*
import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.*


class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
    }

    private var paymentProvider: PaymentProvider? = null

    private var invoiceService = paymentProvider?.let { InvoiceService(dal = dal, paymentProvider = it) }

    private lateinit var pendingInvoice: Invoice
    private lateinit var paidInvoice: Invoice


    @BeforeEach
    fun setup() {
        pendingInvoice =
            Invoice(
                id = 1,
                customerId = 1,
                status = InvoiceStatus.PENDING,
                amount = Money(
                    value = BigDecimal.valueOf(100),
                    currency = Currency.USD
                )
            )

        paidInvoice =
            Invoice(
                id = 1,
                customerId = 1,
                status = InvoiceStatus.PAID,
                amount = Money(
                    value = BigDecimal.valueOf(100),
                    currency = Currency.USD
                )
            )
    }

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService?.fetch(404)
        }
    }

    @Test
    fun payInvoiceTest() {
        paymentProvider = mockk{
            every { charge(pendingInvoice) } returns true
        }
        invoiceService = mockk()
        every { invoiceService!!.payInvoice(pendingInvoice.id) } returns paidInvoice
        val paidInvoice: Invoice? = invoiceService?.payInvoice(pendingInvoice.id)
        assertEquals("PAID", paidInvoice?.status.toString())
    }
}
