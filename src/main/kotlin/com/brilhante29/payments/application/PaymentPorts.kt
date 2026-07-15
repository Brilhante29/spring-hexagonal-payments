package com.brilhante29.payments.application

import com.brilhante29.payments.domain.Payment
import java.util.UUID

interface PaymentRepository {
    fun findById(id: UUID): Payment?
    fun findByIdForUpdate(id: UUID): Payment?
    fun findByIdempotencyKey(key: String): Payment?
    fun insert(payment: Payment, idempotencyKey: String): Boolean
    fun update(payment: Payment)
}

interface TransactionRunner {
    fun <T> required(block: () -> T): T
}
