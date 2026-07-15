# Spec: spring-hexagonal-payments

## Number

#11

## Claim

Idempotent payment authorization and capture preserve domain rules while Spring, JDBC, and PostgreSQL remain replaceable adapters.

## Users And Outcome

- A merchant authorizes a payment with a stable idempotency key.
- A retried identical request receives the original payment.
- A key reused with another payload is rejected.
- An authorized payment can be captured once and replayed safely.

## Scope

In scope: one payment aggregate, authorize/get/capture use cases, PostgreSQL persistence, Flyway migration, REST contract, Testcontainers integration test, Docker demo, k6 benchmark, and core coverage gate.

Out of scope: PCI compliance, ledger, refunds, settlement, chargebacks, authentication, external acquirers, brokers, event sourcing, and multi-region operation.

## Invariants

1. Amount is positive, currency is exactly three uppercase letters, and merchant reference is nonblank.
2. One idempotency key maps to one normalized authorization payload.
3. Authorization starts in `AUTHORIZED`.
4. Capture changes `AUTHORIZED` to `CAPTURED`; replay preserves the original capture.
5. Domain and application import no framework, adapter, transport, database, broker, or cloud SDK.

## Public Contract

- `POST /v1/payments`
- `GET /v1/payments/{id}`
- `POST /v1/payments/{id}/capture`
- `GET /actuator/health`
- OpenAPI source: `api/openapi.yaml`

## Definition Of Done

- [x] One `docker run` starts PostgreSQL, migrates, serves, warms up, benchmarks, prints JSON, and exits.
- [x] README opens with project number and measured p99.
- [x] Baseline and confirmation JSON are committed.
- [x] Core line coverage is at least 75%.
- [x] PostgreSQL behavior is covered with Testcontainers when Docker is available.
- [x] OpenSpec artifacts and reuse review exist.
- [ ] Public CI is green on the published commit.
