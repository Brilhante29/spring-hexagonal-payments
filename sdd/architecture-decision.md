# Architecture Decision

## Decision

Use a single deployable Spring Boot service organized as a hexagonal architecture.

Dependency direction:

```text
HTTP/JDBC/Spring adapters -> application ports and use cases -> domain
```

## Why It Fits

The material risk is not independent deployment. It is preserving idempotency and state-transition rules while transaction, persistence, and transport details remain replaceable and testable. Ports provide useful dependency inversion at those boundaries without introducing distributed coordination.

## Boundaries

- Domain: payment aggregate and invariants.
- Application: authorize, find, capture, repository port, and transaction port.
- Input adapter: Spring MVC REST mapping and error mapping.
- Output adapters: explicit JDBC SQL and Spring transaction runner.
- Composition root: Spring configuration only.
- Verification: architecture import test, unit tests, PostgreSQL integration test, Docker, and k6.

## Principles

- SRP: each boundary owns one reason to change.
- OCP: persistence and transaction implementations can be replaced behind ports.
- LSP: in-memory and JDBC repositories satisfy the behavior consumed by the use case.
- ISP: ports expose only required operations.
- DIP: application code owns the abstractions implemented by adapters.
- KISS: one aggregate, one table, two commands, one process.
- YAGNI: no broker, microservice split, CQRS, event store, or generic payment framework.

## Rejected Styles

- Layered MVC: familiar, but does not itself enforce inward dependencies.
- Microservices: no independent deployment or team boundary justifies network failure modes.
- CQRS/event sourcing: audit replay is outside the measured claim.
- Reactive architecture: JDBC is blocking and the benchmark does not prove an end-to-end nonblocking benefit.
