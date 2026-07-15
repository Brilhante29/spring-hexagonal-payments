# Technical Decisions

## Runtime And Framework

- Java 25, Kotlin 2.4.10, Spring Boot 4.1.0, Gradle 9.3 wrapper.
- Spring MVC matches synchronous REST and blocking PostgreSQL I/O.
- Jackson 3 Kotlin module matches the Spring Boot 4 JSON default.
- Gradle distribution checksum is pinned for wrapper verification.

## Persistence

- PostgreSQL 18.4 is the local and production-compatible store.
- `JdbcClient` keeps `ON CONFLICT DO NOTHING` and `FOR UPDATE` visible.
- Flyway owns the schema; Spring Boot 4 uses its dedicated Flyway starter.
- JDBC writes `Instant` as UTC `OffsetDateTime` to avoid driver-specific timestamp inference.

## API

- REST is selected for stable commands/resources and standard idempotency headers.
- GraphQL is rejected because client-driven field selection does not improve authorize/capture semantics.
- OpenAPI 3.1 is the transport contract.

## Messaging And Cloud

- No Kafka or RabbitMQ: no asynchronous delivery requirement exists.
- No cloud dependency: Docker is the local-first runtime and PostgreSQL is configured at the adapter boundary.
- Kumo is not used because this project calls no AWS API. A later AWS-facing repository must use Kumo locally and retain a real-cloud adapter.

## Testing And Measurement

- JUnit tests domain and use cases without Spring context.
- An architecture test enforces framework-free core packages.
- Testcontainers exercises migration and JDBC semantics against PostgreSQL.
- JaCoCo enforces 75% core line coverage.
- k6 warms up separately, records only measured iterations, exports JSON, and reports p99 explicitly.

## Rejected Libraries

- JPA: obscures the exact atomic insert and row-lock SQL being proved.
- WebFlux/R2DBC: no end-to-end reactive path is present.
- Mock database: insufficient proof for PostgreSQL conflict and timestamp behavior.
- Broker client: would create an unused operational dependency.
