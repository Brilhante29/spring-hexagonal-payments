# #11 spring-hexagonal-payments

**Status:** scaffold

**Proves:** arquitetura hexagonal em pagamentos.

**Benchmark target:** coverage_percent, p99_latency_ms.

**Stack:** java21, spring-boot, postgresql, flyway, testcontainers, k6, docker.

## Next milestone

Implement the smallest Docker-runnable version and produce the first JSON benchmark under enchmarks/results/.

## Run

`ash
docker build -t spring-hexagonal-payments .
docker run --rm spring-hexagonal-payments
`

## Benchmark

`ash
docker run --rm spring-hexagonal-payments benchmark
`

| Metric | Value | Unit |
|---|---:|---|
| coverage_percent, p99_latency_ms | pending | pending |

## Architecture

Defined in sdd/spec.md before implementation.

## References

See REFERENCES.md.