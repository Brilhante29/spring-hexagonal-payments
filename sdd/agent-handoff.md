# Agent Handoff

## Read First

1. `project.yaml`
2. `sdd/spec.md`
3. `sdd/architecture-decision.md`
4. `sdd/technical-decision.md`
5. `api/openapi.yaml`
6. `sdd/reuse-improvement-review.md`
7. `contracts/backend-reliability-platform.yaml`

## Non-Negotiable Rules

- Keep domain and application free of Spring, JDBC, transport, broker, and cloud imports.
- Preserve idempotency under concurrent authorization, not only sequential retries.
- Keep migrations in Flyway and SQL behavior explicit.
- Do not add Kafka, RabbitMQ, GraphQL, WebFlux, JPA, microservices, or cloud SDKs without a new problem force and decision record.
- Keep one-command Docker execution and machine-readable benchmark output.
- Run the OpenSpec plan and reuse review before publication.
- Keep the #14 consumer contract aligned with OpenAPI and preserve private databases.

## Macro Contract

- Provider: `POST /v1/payments` from `api/openapi.yaml`.
- Consumer: `event-sourcing-orders` (#14).
- Lock: `contracts/backend-reliability-platform.yaml`.
- Retry invariant: reuse `order:{orderId}:authorize:v1`.

## Verification

- Windows: `./gradlew.bat test writeCoverage --no-daemon`.
- Linux/macOS: `./gradlew test writeCoverage --no-daemon`.
- Universal: `docker build -t spring-hexagonal-payments .` then `docker run --rm spring-hexagonal-payments`.
- Contract: `powershell -NoProfile -ExecutionPolicy Bypass -File tools/validate-project.ps1`.

## Current Evidence

Status: published

V2 median p99: 108.122 ms, mean throughput: 734.4 req/s, minimum core coverage: 95.65%, zero HTTP failures across three runs. V2 schema validation passed with zero errors. Raw results: benchmarks/results/payments-baseline.json, benchmarks/results/payments-confirmation.json, and benchmarks/results/payments-publication-run-3.json. Publication result: benchmarks/publication/payments-baseline-v2.json. Exact-head CI: https://github.com/Brilhante29/spring-hexagonal-payments/actions/runs/30581889788.
