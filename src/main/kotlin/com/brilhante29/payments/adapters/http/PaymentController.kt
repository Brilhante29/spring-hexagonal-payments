package com.brilhante29.payments.adapters.http

import com.brilhante29.payments.application.AuthorizePaymentCommand
import com.brilhante29.payments.application.PaymentService
import com.brilhante29.payments.domain.Payment
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class AuthorizePaymentRequest(
    @field:Positive val amountMinor: Long,
    @field:Pattern(regexp = "^[A-Za-z]{3}$") val currency: String,
    @field:NotBlank @field:Size(max = 64) val merchantReference: String,
)

data class PaymentResponse(
    val id: UUID,
    val amountMinor: Long,
    val currency: String,
    val merchantReference: String,
    val status: String,
    val createdAt: Instant,
    val capturedAt: Instant?,
    val replayed: Boolean? = null,
)

@RestController
@RequestMapping("/v1/payments")
class PaymentController(private val service: PaymentService) {
    @PostMapping
    fun authorize(
        @RequestHeader("Idempotency-Key") idempotencyKey: String,
        @Valid @RequestBody request: AuthorizePaymentRequest,
    ): ResponseEntity<PaymentResponse> {
        val result = service.authorize(
            AuthorizePaymentCommand(idempotencyKey, request.amountMinor, request.currency, request.merchantReference),
        )
        val status = if (result.replayed) HttpStatus.OK else HttpStatus.CREATED
        return ResponseEntity.status(status).body(result.payment.toResponse(result.replayed))
    }

    @GetMapping("/{id}")
    fun find(@PathVariable id: UUID): PaymentResponse = service.find(id).toResponse()

    @PostMapping("/{id}/capture")
    fun capture(@PathVariable id: UUID): PaymentResponse = service.capture(id).toResponse()
}

private fun Payment.toResponse(replayed: Boolean? = null) = PaymentResponse(
    id, amountMinor, currency, merchantReference, status.name, createdAt, capturedAt, replayed,
)
