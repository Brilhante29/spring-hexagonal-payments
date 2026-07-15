package com.brilhante29.payments.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class PaymentTest {
    private val now = Instant.parse("2026-07-15T00:00:00Z")

    @Test fun `authorizes and captures a valid payment`() {
        val payment = Payment.authorize(UUID.randomUUID(), 2590, "BRL", "order-42", now)
        val captured = payment.capture(now.plusSeconds(1))
        assertEquals(PaymentStatus.AUTHORIZED, payment.status)
        assertEquals(PaymentStatus.CAPTURED, captured.status)
        assertEquals(now.plusSeconds(1), captured.capturedAt)
    }

    @Test fun `capture is idempotent`() {
        val captured = Payment.authorize(UUID.randomUUID(), 100, "USD", "order-1", now).capture(now)
        assertSame(captured, captured.capture(now.plusSeconds(5)))
    }

    @Test fun `rejects invalid money and currency`() {
        assertThrows(InvalidPayment::class.java) { Payment.authorize(UUID.randomUUID(), 0, "BRL", "order-1", now) }
        assertThrows(InvalidPayment::class.java) { Payment.authorize(UUID.randomUUID(), 100, "real", "order-1", now) }
    }

    @Test fun `rejects invalid merchant reference and inconsistent captured state`() {
        assertThrows(InvalidPayment::class.java) {
            Payment.authorize(UUID.randomUUID(), 100, "BRL", " ", now)
        }
        assertThrows(InvalidPayment::class.java) {
            Payment(
                UUID.randomUUID(),
                100,
                "BRL",
                "order-1",
                PaymentStatus.CAPTURED,
                now,
            )
        }
    }
}
