package com.brilhante29.payments.adapters.http

import com.brilhante29.payments.application.IdempotencyConflict
import com.brilhante29.payments.application.PaymentNotFound
import com.brilhante29.payments.application.PaymentRepository
import com.brilhante29.payments.application.PaymentService
import com.brilhante29.payments.application.TransactionRunner
import com.brilhante29.payments.domain.InvalidPayment
import com.brilhante29.payments.domain.Payment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class PaymentControllerTest {
    private val repository = HttpTestPaymentRepository()
    private val id = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private val clock = Clock.fixed(Instant.parse("2026-07-15T15:00:00Z"), ZoneOffset.UTC)
    private val service = PaymentService(repository, HttpTestTransactions, clock) { id }
    private val controller = PaymentController(service)

    @Test fun `returns created first and ok on idempotent replay`() {
        val request = AuthorizePaymentRequest(2590, "brl", "order-http")

        val created = controller.authorize("http-idem", request)
        val replay = controller.authorize("http-idem", request)

        assertEquals(HttpStatus.CREATED, created.statusCode)
        assertEquals(HttpStatus.OK, replay.statusCode)
        assertEquals(false, created.body?.replayed)
        assertEquals(true, replay.body?.replayed)
        assertEquals("BRL", created.body?.currency)
    }

    @Test fun `finds and captures through the public controller contract`() {
        controller.authorize("capture-idem", AuthorizePaymentRequest(100, "USD", "order-capture"))

        val found = controller.find(id)
        val captured = controller.capture(id)

        assertEquals("AUTHORIZED", found.status)
        assertEquals("CAPTURED", captured.status)
        assertNotNull(captured.capturedAt)
    }
}

class ApiExceptionHandlerTest {
    private val handler = ApiExceptionHandler()
    private val id = UUID.fromString("33333333-3333-3333-3333-333333333333")

    @Test fun `maps domain failures to stable api errors`() {
        val notFound = handler.notFound(PaymentNotFound(id))
        val conflict = handler.conflict(IdempotencyConflict())
        val invalid = handler.invalid(InvalidPayment("bad payment"))

        assertEquals(HttpStatus.NOT_FOUND, notFound.statusCode)
        assertEquals("payment_not_found", notFound.body?.code)
        assertEquals(HttpStatus.CONFLICT, conflict.statusCode)
        assertEquals("idempotency_conflict", conflict.body?.code)
        assertEquals(HttpStatus.BAD_REQUEST, invalid.statusCode)
        assertEquals("invalid_request", invalid.body?.code)
    }
}

private object HttpTestTransactions : TransactionRunner {
    override fun <T> required(block: () -> T): T = block()
}

private class HttpTestPaymentRepository : PaymentRepository {
    private val payments = linkedMapOf<UUID, Payment>()
    private val keys = linkedMapOf<String, UUID>()

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
        payments[payment.id] = payment
    }
}
