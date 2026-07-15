package com.brilhante29.payments.adapters.config

import com.brilhante29.payments.adapters.persistence.JdbcPaymentRepository
import com.brilhante29.payments.application.PaymentRepository
import com.brilhante29.payments.application.PaymentService
import com.brilhante29.payments.application.TransactionRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock

@Configuration
class PaymentConfiguration {
    @Bean
    fun paymentRepository(jdbc: JdbcClient): PaymentRepository = JdbcPaymentRepository(jdbc)

    @Bean
    fun transactionRunner(manager: PlatformTransactionManager): TransactionRunner {
        val template = TransactionTemplate(manager)
        return object : TransactionRunner {
            override fun <T> required(block: () -> T): T = template.execute { block() }!!
        }
    }

    @Bean
    fun paymentService(repository: PaymentRepository, transactions: TransactionRunner): PaymentService =
        PaymentService(repository, transactions, Clock.systemUTC())
}
