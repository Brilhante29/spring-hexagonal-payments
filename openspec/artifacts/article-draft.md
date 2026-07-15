# #11 spring-hexagonal-payments: p99_latency_ms = 131.41 milliseconds

Idempotent payment authorization and capture preserve domain rules while Spring, JDBC, and PostgreSQL remain replaceable adapters.

This repository belongs to the Backend Reliability and Architecture Platform program. Its job is narrow: prove the measurable claim through the selected component pack before adding unrelated infrastructure or features.

The benchmark is the proof. p99_latency_ms = 131.41 milliseconds.  The result is stored in `benchmarks/results/payments-baseline.json` and can be reproduced from the Docker/local path.

The important architecture decision is hexagonal. Payment invariants must remain independent while HTTP, transactions, and PostgreSQL are concrete replaceable boundaries.

The default path stays local-first. The project uses spring-kotlin-backend, exposes rest-http, uses messaging mode `none`, and stores data with `postgresql`. The dependency rule is explicit: Input and output adapters depend inward on application ports and domain types; domain and application import no adapter or framework.

The rejected work matters as much as the implemented work. Anything that does not improve the benchmark stays out of the first version.

Post angle: start with the number, show the architecture boundary, then explain which future adapter can be added without changing the core use cases.
