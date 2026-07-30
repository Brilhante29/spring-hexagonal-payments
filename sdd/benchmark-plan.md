# Benchmark Plan

## Hypothesis

The service can preserve idempotent authorization semantics at a reproducible p99 with zero HTTP failures while keeping core line coverage above 75%.

## Primary Metric

`p99_latency_ms`, lower is better. Secondary evidence is throughput, failure rate, check rate, measured requests, and core line coverage.

## Reproduction

Windows:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/benchmark.ps1
```

Linux or macOS:

```bash
./tools/benchmark.sh
```

Universal demo:

```text
docker build -t spring-hexagonal-payments .
docker run --rm spring-hexagonal-payments
```

## Controlled Inputs

- 32 virtual users.
- 10-second measured window.
- 200 setup authorizations excluded from measured throughput and latency.
- Unique idempotency key and merchant reference per measured iteration.
- One ephemeral PostgreSQL 18.4 instance.
- The k6 summary includes `p(99)` because custom p99 is not in the default summary trend set.

## Acceptance Gates

- HTTP failure rate equals 0.
- Check rate equals 1.
- p99 is below 250 ms.
- Core line coverage is at least 75%.
- V2 records three independent runs; its p99 publication value is the median and its throughput is the arithmetic mean.
- V1 result conforms to `.portfolio/contracts/benchmark-result.schema.json` and V2 result conforms to `contracts/benchmark-result-v2.schema.json`.

## Results

| Run | p99 ms | req/s | Requests | Failures | Core coverage |
|---|---:|---:|---:|---:|---:|
| V2 run 1 | 87.201 | 801.2 | 8,012 | 0 | 95.65% |
| V2 run 2 | 108.122 | 756.3 | 7,563 | 0 | 95.65% |

V2 publication result: `benchmarks/publication/payments-baseline-v2.json`. It records three samples and provenance digests.
