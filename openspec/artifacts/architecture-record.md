# Architecture Record: spring-hexagonal-payments

## Decision

- Architecture: `hexagonal`
- Stack profile: `spring-kotlin-backend`
- API style: `rest-http`
- Messaging: `none`
- Database/runtime: `postgresql` / `Java 25 JRE with Kotlin 2.4.10 and Spring Boot 4.1.0`

## Reason

Payment invariants must remain independent while HTTP, transactions, and PostgreSQL are concrete replaceable boundaries.

## Dependency Direction

Input and output adapters depend inward on application ports and domain types; domain and application import no adapter or framework.

## Boundaries

- payment domain aggregate and invariants
- authorization, lookup, and capture use cases
- repository and transaction output ports
- Spring MVC input adapter
- JDBC and Spring transaction output adapters
- Docker and k6 verification harness

## Library Policy

Spring MVC matches blocking JDBC; JdbcClient keeps SQL explicit; Flyway owns schema evolution; Testcontainers uses real PostgreSQL; k6 owns load thresholds.

## Principle Check

- SRP: keep benchmark, API, use cases, and adapters separate.
- OCP: new providers must be adapters, not domain rewrites.
- LSP: replacement providers must preserve observable behavior.
- ISP: ports stay narrow.
- DIP: application depends on behavior, not infrastructure.
- KISS/YAGNI: leave out anything that does not improve the benchmark.
