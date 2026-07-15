package com.brilhante29.payments.adapters.persistence

import com.brilhante29.payments.domain.Payment
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.JdbcClient
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Instant
import java.util.UUID

@Testcontainers(disabledWithoutDocker = true)
class JdbcPaymentRepositoryTest {
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:18.4-alpine"))

        private lateinit var repository: JdbcPaymentRepository

        @BeforeAll
        @JvmStatic
        fun migrate() {
            Flyway.configure().dataSource(postgres.jdbcUrl, postgres.username, postgres.password).load().migrate()
            val dataSource = org.postgresql.ds.PGSimpleDataSource().apply {
                setURL(postgres.jdbcUrl)
                user = postgres.username
                password = postgres.password
            }
            repository = JdbcPaymentRepository(JdbcClient.create(JdbcTemplate(dataSource)))
        }
    }

    @Test fun `persists, finds, captures, and prevents duplicate idempotency keys`() {
        val payment = Payment.authorize(
            UUID.randomUUID(),
            2590,
            "BRL",
            "order-db",
            Instant.parse("2026-07-15T12:00:00.123456Z"),
        )
        assertTrue(repository.insert(payment, "db-idem"))
        assertFalse(repository.insert(payment.copy(id = UUID.randomUUID()), "db-idem"))
        assertEquals(payment, repository.findByIdempotencyKey("db-idem"))
        assertEquals(payment, repository.findById(payment.id))
        assertEquals(payment, repository.findByIdForUpdate(payment.id))

        val captured = payment.capture(payment.createdAt.plusSeconds(1))
        repository.update(captured)
        assertEquals(captured, repository.findById(payment.id))
        assertNull(repository.findById(UUID.randomUUID()))
    }
}
