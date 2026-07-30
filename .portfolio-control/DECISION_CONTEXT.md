# Decision Context: #11 spring-hexagonal-payments

## Problem

Prove idempotent payment authorization and capture at a measured HTTP p99 with
explicit PostgreSQL transactions, while keeping domain policy independent from
Spring, JDBC, and transport.

## Architecture Decision

Use hexagonal architecture with Kotlin application/domain ports and Spring MVC,
Spring JDBC, Flyway, and PostgreSQL adapters. The workload is synchronous and
command-oriented, so REST is enough; GraphQL, Kafka, RabbitMQ, WebFlux, JPA,
and microservices remain rejected until a measured force requires them.

## Reuse Decision

Consume the kit's manifest v2, separate V1/V2 benchmark contracts, exact-head
publication evidence, and continuation protocol. Add Gradle dependency locking
and a PowerShell V2 producer because the JVM family needs the same provenance
quality as the Python RAG project.

## Evidence Boundary

The V1 JSON is the raw k6 execution output. The V2 JSON is the publication
contract and includes workload/config digests, locked dependency digest, Docker
image digest, source commit, and runtime metadata. No throughput or p99 claim is
valid without rerunning the Docker command.

## Principles

- SRP: domain rules, use cases, ports, adapters, and benchmark harness have separate reasons to change.
- OCP/DIP: Spring/JDBC can be replaced behind application ports.
- LSP/ISP: repository and transaction contracts remain narrow and substitutable.
- KISS/YAGNI: one aggregate and one synchronous boundary; no broker or cloud emulation.
- LIsP: payment state transitions preserve the Payment abstraction's invariants.