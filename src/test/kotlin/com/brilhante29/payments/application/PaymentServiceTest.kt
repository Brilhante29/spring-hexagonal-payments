package com.brilhante29.payments.application

import com.brilhante29.payments.domain.Payment
import com.brilhante29.payments.domain.PaymentStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PaymentServiceTest {
    private val repository = InMemoryPaymentRepository()
    private val now = Instant.parse("2026-07-15T12:00:00Z")
    private val paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val service = PaymentService(repository, DirectTransactionRunner, Clock.fixed(now, ZoneOffset.UTC)) { paymentId }

    @Test fun `replays the same idempotent authorization`() {
        val command = AuthorizePaymentCommand("idem-1", 2590, "brl", "order-42")
        val first = service.authorize(command)
        val replay = service.authorize(command)
        assertFalse(first.replayed)
        assertTrue(replay.replayed)
        assertEquals(first.payment, replay.payment)
        assertEquals("BRL", first.payment.currency)
    }

    @Test fun `rejects idempotency key reused with another payload`() {
        service.authorize(AuthorizePaymentCommand("idem-2", 100, "USD", "order-a"))
        assertThrows(IdempotencyConflict::class.java) {
            service.authorize(AuthorizePaymentCommand("idem-2", 200, "USD", "order-a"))
        }
    }

    @Test fun `captures and finds payment`() {
        val authorized = service.authorize(AuthorizePaymentCommand("idem-3", 100, "USD", "order-b")).payment
        val captured = service.capture(authorized.id)
        assertEquals(PaymentStatus.CAPTURED, captured.status)
        assertEquals(captured, service.find(authorized.id))
    }

    @Test fun `fails when payment does not exist`() {
        assertThrows(PaymentNotFound::class.java) { service.find(UUID.randomUUID()) }
        assertThrows(PaymentNotFound::class.java) { service.capture(UUID.randomUUID()) }
    }

    @Test fun `rejects malformed idempotency key`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.authorize(AuthorizePaymentCommand("bad key", 100, "USD", "order-c"))
        }
    }

    @Test fun `handles a concurrent insert as an idempotent replay`() {
        val winner = Payment.authorize(paymentId, 100, "USD", "order-race", now)
        val racingService = PaymentService(
            RacingPaymentRepository(winner),
            DirectTransactionRunner,
            Clock.fixed(now, ZoneOffset.UTC),
        ) { paymentId }

        val result = racingService.authorize(AuthorizePaymentCommand("idem-race", 100, "usd", "order-race"))

        assertTrue(result.replayed)
        assertEquals(winner, result.payment)
    }

    @Test fun `fails closed when a concurrent winner cannot be read`() {
        val brokenService = PaymentService(
            RacingPaymentRepository(null),
            DirectTransactionRunner,
            Clock.fixed(now, ZoneOffset.UTC),
        ) { paymentId }

        assertThrows(IllegalStateException::class.java) {
            brokenService.authorize(AuthorizePaymentCommand("idem-race", 100, "USD", "order-race"))
        }
    }

    @Test fun `does not persist a second update when capture is replayed`() {
        service.authorize(AuthorizePaymentCommand("idem-capture", 100, "USD", "order-capture"))
        service.capture(paymentId)
        service.capture(paymentId)
        assertEquals(1, repository.updateCount)
    }
}

private object DirectTransactionRunner : TransactionRunner {
    override fun <T> required(block: () -> T): T = block()
}

private class InMemoryPaymentRepository : PaymentRepository {
    private val payments = linkedMapOf<UUID, Payment>()
    private val keys = linkedMapOf<String, UUID>()
    var updateCount = 0

    override fun findById(id: UUID) = payments[id]
    override fun findByIdForUpdate(id: UUID) = payments[id]
    override fun findByIdempotencyKey(key: String) = keys[key]?.let(payments::get)

    override fun insert(payment: Payment, idempotencyKey: String): Boolean {
        if (keys.containsKey(idempotencyKey)) return false
        payments[payment.id] = payment
        keys[idempotencyKey] = payment.id
        return true
    }

    override fun update(payment: Payment) {
        updateCount++
        payments[payment.id] = payment
    }
}

private class RacingPaymentRepository(private val winner: Payment?) : PaymentRepository {
    override fun findById(id: UUID): Payment? = null
    override fun findByIdForUpdate(id: UUID): Payment? = null
    override fun findByIdempotencyKey(key: String): Payment? = winner
    override fun insert(payment: Payment, idempotencyKey: String): Boolean = false
    override fun update(payment: Payment) = Unit
}
