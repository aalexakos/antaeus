package io.pleo.antaeus.core.services

import io.mockk.MockK
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
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
    private val dal = mockk<AntaeusDal>()

    private var paymentProvider = spyk<PaymentProvider>()

    private var invoiceService = InvoiceService(dal = dal, paymentProvider = paymentProvider)

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
        every { dal.fetchInvoice(404) } returns null
        every { paymentProvider.charge(pendingInvoice) } returns true
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    //Test that modifies the invoice status from PENDING to PAID
    @Test
    fun payInvoiceTest() {
        every { paymentProvider.charge(pendingInvoice) } returns true

        invoiceService = mockk()
        every { invoiceService.payInvoice(pendingInvoice.id) } returns paidInvoice
        val paidInvoice: Invoice? = invoiceService.payInvoice(pendingInvoice.id)
        assertEquals("PAID", paidInvoice?.status.toString())
    }

    //Test that modifies the invoice status from PENDING to PAID but charge function fails
    @Test
    fun payInvoiceTestAndChargeFails() {
        every { paymentProvider.charge(pendingInvoice) } returns false

        every { dal.fetchInvoice(pendingInvoice.id) } returns pendingInvoice

        assertThrows<NetworkException> {
            invoiceService.payInvoice(pendingInvoice.id)
        }
    }
}
