# Agent Handoff

## Read First

1. `project.yaml`
2. `sdd/spec.md`
3. `sdd/architecture-decision.md`
4. `sdd/technical-decision.md`
5. `api/openapi.yaml`
6. `sdd/reuse-improvement-review.md`

## Non-Negotiable Rules

- Keep domain and application free of Spring, JDBC, transport, broker, and cloud imports.
- Preserve idempotency under concurrent authorization, not only sequential retries.
- Keep migrations in Flyway and SQL behavior explicit.
- Do not add Kafka, RabbitMQ, GraphQL, WebFlux, JPA, microservices, or cloud SDKs without a new problem force and decision record.
- Keep one-command Docker execution and machine-readable benchmark output.
- Run the OpenSpec plan and reuse review before publication.

## Verification

- Windows: `./gradlew.bat test writeCoverage --no-daemon`.
- Linux/macOS: `./gradlew test writeCoverage --no-daemon`.
- Universal: `docker build -t spring-hexagonal-payments .` then `docker run --rm spring-hexagonal-payments`.
- Contract: `powershell -NoProfile -ExecutionPolicy Bypass -File tools/validate-project.ps1`.

## Current Evidence

Baseline: 131.414 ms p99, 758.3 req/s, 95.65% core line coverage, zero HTTP failures. Confirmation variance is below 2.1% for p99 and throughput.
