## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!


## Challenge Execution 

### Introduction 
The main idea of this hand over, is that invoices will be "sent" to customers periodically every 1st of the month. In this example, the "sent operation" will be just a print of the invoices with status "PENDING".
In a real world scenario, instead of just a print, the "PENDING" invoices would be sent to each customer through their e-mail. 
Thus, the additions to the existing code, is a new endpoint that returns the "PENDING" invoices and also a modification to the Dockerfile to add a **Cron Job** for monthly scheduling.

### Endpoint and Database
The new endpoint's URL is: _/rest/v1/invoices/payment_. This endpoint will be mainly used by the monthly scheduler 
to print the pending invoices. Beside this endpoint, a few others were created as alternatives for different working cases.
This cases can be, for example, checking if an invoice status has value "PAID" or "PENDING" (_/rest/v1/invoices/payment/{:id}_).
Or an other case, to get all the invoices for a customer (_/rest/v1/invoices/payment/{:id}_). 
All the above new endpoints may come with new database transactions. In the endpoint that is used for the scheduler, a new transaction was added:
```
fun fetchUnpaidInvoices(): List<Invoice> {
    return transaction(db) {
        InvoiceTable
        .select { InvoiceTable.status.eq("PENDING") }
        .map { it.toInvoice() }
    }
}
```
### Scheduler 
The main idea behind the scheduler is that the webapp will run in a docker container. In the same container, a **Cron Job** will be executing 
a monthly API REST call to the endpoint that was created before.

Files:
* curl.sh: contains the curl request,
* cron-job: describes the Cron Job execution period and points to the _curl.sh_ for the code to be executed

Note: Current Cron Job is set to run every 1 minute, just for testing purposes. For execution every month on the last day of the month, at noon (:P), change cron-job file to contain **0 0 12 L * ?**.
