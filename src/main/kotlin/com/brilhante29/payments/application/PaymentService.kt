package com.brilhante29.payments.application

import com.brilhante29.payments.domain.Payment
import java.time.Clock
import java.util.UUID

data class AuthorizePaymentCommand(
    val idempotencyKey: String,
    val amountMinor: Long,
    val currency: String,
    val merchantReference: String,
)

data class AuthorizationResult(val payment: Payment, val replayed: Boolean)

class PaymentNotFound(id: UUID) : RuntimeException("payment $id was not found")
class IdempotencyConflict : RuntimeException("idempotency key was already used with a different request")

class PaymentService(
    private val repository: PaymentRepository,
    private val transactions: TransactionRunner,
    private val clock: Clock,
    private val newId: () -> UUID = UUID::randomUUID,
) {
    fun authorize(command: AuthorizePaymentCommand): AuthorizationResult = transactions.required {
        require(command.idempotencyKey.matches(Regex("^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$"))) {
            "invalid idempotency key"
        }
        repository.findByIdempotencyKey(command.idempotencyKey)?.let {
            return@required AuthorizationResult(assertSameRequest(it, command), true)
        }

        val candidate = Payment.authorize(
            id = newId(),
            amountMinor = command.amountMinor,
            currency = command.currency.uppercase(),
            merchantReference = command.merchantReference,
            at = clock.instant(),
        )
        if (repository.insert(candidate, command.idempotencyKey)) {
            AuthorizationResult(candidate, false)
        } else {
            val concurrent = repository.findByIdempotencyKey(command.idempotencyKey)
                ?: error("idempotency conflict did not return the existing payment")
            AuthorizationResult(assertSameRequest(concurrent, command), true)
        }
    }

    fun find(id: UUID): Payment = repository.findById(id) ?: throw PaymentNotFound(id)

    fun capture(id: UUID): Payment = transactions.required {
        val current = repository.findByIdForUpdate(id) ?: throw PaymentNotFound(id)
        val captured = current.capture(clock.instant())
        if (captured !== current) repository.update(captured)
        captured
    }

    private fun assertSameRequest(payment: Payment, command: AuthorizePaymentCommand): Payment {
        if (payment.amountMinor != command.amountMinor ||
            payment.currency != command.currency.uppercase() ||
            payment.merchantReference != command.merchantReference
        ) throw IdempotencyConflict()
        return payment
    }
}
