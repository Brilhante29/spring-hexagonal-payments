package com.brilhante29.payments.domain

import java.time.Instant
import java.util.UUID

enum class PaymentStatus { AUTHORIZED, CAPTURED }

class InvalidPayment(message: String) : RuntimeException(message)
class InvalidPaymentTransition(message: String) : RuntimeException(message)

data class Payment(
    val id: UUID,
    val amountMinor: Long,
    val currency: String,
    val merchantReference: String,
    val status: PaymentStatus,
    val createdAt: Instant,
    val capturedAt: Instant? = null,
) {
    init {
        if (amountMinor <= 0) throw InvalidPayment("amount_minor must be greater than zero")
        if (!currency.matches(Regex("^[A-Z]{3}$"))) throw InvalidPayment("currency must be a three-letter uppercase code")
        if (merchantReference.isBlank() || merchantReference.length > 64) {
            throw InvalidPayment("merchant_reference must contain 1 to 64 characters")
        }
        if (status == PaymentStatus.CAPTURED && capturedAt == null) {
            throw InvalidPayment("captured payment requires captured_at")
        }
    }

    fun capture(at: Instant): Payment = when (status) {
        PaymentStatus.AUTHORIZED -> copy(status = PaymentStatus.CAPTURED, capturedAt = at)
        PaymentStatus.CAPTURED -> this
    }

    companion object {
        fun authorize(
            id: UUID,
            amountMinor: Long,
            currency: String,
            merchantReference: String,
            at: Instant,
        ) = Payment(id, amountMinor, currency, merchantReference, PaymentStatus.AUTHORIZED, at)
    }
}
