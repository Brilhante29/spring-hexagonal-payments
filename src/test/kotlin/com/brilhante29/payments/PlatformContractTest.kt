package com.brilhante29.payments

import kotlin.io.path.Path
import kotlin.io.path.readText
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlatformContractTest {
    @Test
    fun `platform contract stays aligned with OpenAPI`() {
        val openApi = Path("api/openapi.yaml").readText()
        val platform = Path("contracts/backend-reliability-platform.yaml").readText()

        assertTrue(openApi.contains("/v1/payments:"))
        assertTrue(openApi.contains("Idempotency-Key"))
        assertTrue(openApi.contains("amount_minor"))
        assertTrue(openApi.contains("currency"))
        assertTrue(openApi.contains("merchant_reference"))
        assertTrue(platform.contains("operation: POST /v1/payments"))
        assertTrue(platform.contains("project: event-sourcing-orders"))
        assertTrue(platform.contains("consumer_must_retry_with_same_idempotency_key: true"))
    }
}
