package com.brilhante29.payments.adapters.persistence

import com.brilhante29.payments.application.PaymentRepository
import com.brilhante29.payments.domain.Payment
import com.brilhante29.payments.domain.PaymentStatus
import org.springframework.jdbc.core.simple.JdbcClient
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class JdbcPaymentRepository(private val jdbc: JdbcClient) : PaymentRepository {
    override fun findById(id: UUID): Payment? = queryOne(
        "SELECT * FROM payments WHERE id = :id",
        mapOf("id" to id),
    )

    override fun findByIdForUpdate(id: UUID): Payment? = queryOne(
        "SELECT * FROM payments WHERE id = :id FOR UPDATE",
        mapOf("id" to id),
    )

    override fun findByIdempotencyKey(key: String): Payment? = queryOne(
        "SELECT * FROM payments WHERE idempotency_key = :key",
        mapOf("key" to key),
    )

    override fun insert(payment: Payment, idempotencyKey: String): Boolean = jdbc.sql(
        """
        INSERT INTO payments (
          id, idempotency_key, amount_minor, currency, merchant_reference, status, created_at, captured_at
        ) VALUES (
          :id, :key, :amount, :currency, :merchantReference, :status, :createdAt, :capturedAt
        ) ON CONFLICT (idempotency_key) DO NOTHING
        """.trimIndent(),
    ).params(
        mapOf(
            "id" to payment.id,
            "key" to idempotencyKey,
            "amount" to payment.amountMinor,
            "currency" to payment.currency,
            "merchantReference" to payment.merchantReference,
            "status" to payment.status.name,
            "createdAt" to payment.createdAt.atOffset(ZoneOffset.UTC),
            "capturedAt" to payment.capturedAt?.atOffset(ZoneOffset.UTC),
        ),
    ).update() == 1

    override fun update(payment: Payment) {
        val changed = jdbc.sql(
            """
            UPDATE payments
            SET status = :status, captured_at = :capturedAt, version = version + 1
            WHERE id = :id
            """.trimIndent(),
        ).params(
            mapOf("id" to payment.id, "status" to payment.status.name, "capturedAt" to payment.capturedAt?.atOffset(ZoneOffset.UTC)),
        ).update()
        check(changed == 1) { "payment update affected $changed rows" }
    }

    private fun queryOne(sql: String, parameters: Map<String, Any>): Payment? = jdbc.sql(sql)
        .params(parameters)
        .query(::mapPayment)
        .optional()
        .orElse(null)

    private fun mapPayment(rs: ResultSet, @Suppress("UNUSED_PARAMETER") row: Int) = Payment(
        id = rs.getObject("id", UUID::class.java),
        amountMinor = rs.getLong("amount_minor"),
        currency = rs.getString("currency"),
        merchantReference = rs.getString("merchant_reference"),
        status = PaymentStatus.valueOf(rs.getString("status")),
        createdAt = rs.getObject("created_at", OffsetDateTime::class.java).toInstant(),
        capturedAt = rs.getObject("captured_at", OffsetDateTime::class.java)?.toInstant(),
    )
}
