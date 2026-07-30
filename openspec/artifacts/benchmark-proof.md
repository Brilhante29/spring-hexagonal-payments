# Benchmark Proof: spring-hexagonal-payments

## Primary Metric

- Metric: `p99_latency_ms`
- Unit: `milliseconds`
- Result: V2 median p99_latency_ms = 108.122 milliseconds across three independent Docker runs
- V1 result path: `benchmarks/results/payments-baseline.json`

## Command

    powershell -NoProfile -ExecutionPolicy Bypass -File tools/benchmark-v2.ps1

## Evidence



The README/post number must come from the committed benchmark JSON, not from manual text.
