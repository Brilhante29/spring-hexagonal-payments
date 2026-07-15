package com.brilhante29.payments.adapters.http

import com.brilhante29.payments.application.IdempotencyConflict
import com.brilhante29.payments.application.PaymentNotFound
import com.brilhante29.payments.domain.InvalidPayment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ApiError(val code: String, val message: String)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(PaymentNotFound::class)
    fun notFound(error: PaymentNotFound) = response(HttpStatus.NOT_FOUND, "payment_not_found", error.message)

    @ExceptionHandler(IdempotencyConflict::class)
    fun conflict(error: IdempotencyConflict) = response(HttpStatus.CONFLICT, "idempotency_conflict", error.message)

    @ExceptionHandler(InvalidPayment::class, IllegalArgumentException::class, MethodArgumentNotValidException::class)
    fun invalid(error: Exception) = response(HttpStatus.BAD_REQUEST, "invalid_request", error.message)

    private fun response(status: HttpStatus, code: String, message: String?) =
        ResponseEntity.status(status).body(ApiError(code, message ?: code))
}
