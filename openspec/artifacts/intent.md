# Intent: spring-hexagonal-payments

## Measurable Claim

Idempotent payment authorization and capture preserve domain rules while Spring, JDBC, and PostgreSQL remain replaceable adapters.

## Problem

Provides the synchronous money-movement boundary used by later outbox, saga, event-sourcing, gateway, and observability projects.

## In Scope

- Use the selected component pack: `backend-reliability-platform`.
- Keep the project under the Backend Reliability and Architecture Platform program.
- Preserve the benchmark contract: `p99_latency_ms` in `benchmarks/results/payments-baseline.json`.
- Keep the default path local-first and reproducible.

## Out Of Scope

- Paid credentials for the default demo.
- External infrastructure that is not required by the benchmark.
- Replacing local portfolio skills with external components silently.

## Default Demo Path

- Status: published
- Runtime: Java 25 JRE with Kotlin 2.4.10 and Spring Boot 4.1.0
- Benchmark command: `powershell -NoProfile -ExecutionPolicy Bypass -File tools/benchmark.ps1`

## Public Proof

- Benchmark: p99_latency_ms = 131.41 milliseconds
- Result path: `benchmarks/results/payments-baseline.json`
