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
- Baseline and confirmation p99 differ by at most 15%.
- Result conforms to `.portfolio/contracts/benchmark-result.schema.json`.

## Results

| Run | p99 ms | req/s | Requests | Failures | Core coverage |
|---|---:|---:|---:|---:|---:|
| Baseline | 131.414 | 758.3 | 7,583 | 0 | 95.65% |
| Confirmation | 134.165 | 772.8 | 7,728 | 0 | 95.65% |

P99 variance: 2.09%. Throughput variance: 1.91%.
