package seg3x02.carhms.application.services

interface TransactionRunner {
    fun <T> run(action: () -> T): T
}
