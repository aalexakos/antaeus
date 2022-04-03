/*
    Configures the rest app along with basic exception handling and URL endpoints.
 */

package io.pleo.antaeus.rest

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.pleo.antaeus.core.exceptions.EntityNotFoundException
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private val thisFile: () -> Unit = {}

class AntaeusRest(
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) : Runnable {

    override fun run() {
        app.start(7000)
    }

    // Set up Javalin rest app
    private val app = Javalin
        .create()
        .apply {
            // InvoiceNotFoundException: return 404 HTTP status code
            exception(EntityNotFoundException::class.java) { _, ctx ->
                ctx.status(404)
            }
            // Unexpected exception: return HTTP 500
            exception(Exception::class.java) { e, _ ->
                logger.error(e) { "Internal server error" }
            }
            // On 404: return message
            error(404) { ctx -> ctx.json("not found") }
        }

    init {
        // Set up URL endpoints for the rest app
        app.routes {
            get("/") {
                it.result("Welcome to Antaeus! see AntaeusRest class for routes")
            }
            path("rest") {
                // Route to check whether the app is running
                // URL: /rest/health
                get("health") {
                    it.json("ok")
                }

                // V1
                path("v1") {
                    path("invoices") {
                        // URL: /rest/v1/invoices
                        get {
                            it.json(invoiceService.fetchAll())
                        }

                        // URL: /rest/v1/invoices/payment
                        path ("payment") {
                            get {
                                val invoices: List<Invoice> = invoiceService.pay()
                                it.json(invoices)
                            }
                            // URL: /rest/v1/invoices/payment/{:id}
                            get (":id"){
                                val invoice: Invoice = invoiceService.payInvoice(it.pathParam("id").toInt())!!
                                //Just a pretty message for invoice status. In real world usage i would just return 0 or 1 for the status
                                if (invoice.status.equals("PENDING")) it.result("Invoice with id: " + invoice.id + " needs to be paid the amount of " +invoice.amount.value+ " " +invoice.amount.currency)
                                else it.result("Invoice with id: " + invoice.id + " is paid")
                            }
                        }

                        // URL: /rest/v1/invoices/{:id}
                        get(":id") {
                            it.json(invoiceService.fetch(it.pathParam("id").toInt()))
                        }
                    }

                    path("customers") {
                        // URL: /rest/v1/customers
                        get {
                            logger.info { "done!" }
                            it.json(customerService.fetchAll())
                        }
                        path ("invoices"){
                            // URL: /rest/v1/customers/invoices/{:id}
                            get(":id"){
                                customerService.fetchCustomerInvoices(it.pathParam("id").toInt())
                                    ?.let { it1 -> it.json(it1) }
                            }
                        }

                        // URL: /rest/v1/customers/{:id}
                        get(":id") {
                            it.json(customerService.fetch(it.pathParam("id").toInt()))
                        }
                    }
                }
            }
        }
    }
}
