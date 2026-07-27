# Quality Gates: #11 spring-hexagonal-payments

Completion requires evidence, not intent.

- [x] README opens with `#11 <name>` and reports the current benchmark number.
      README title: "#11 spring-hexagonal-payments: 131.414 ms p99 at 758.3 req/s".
- [x] `project.yaml` names the problem, architecture, stack, primary metric, and result path.
      primary_metric: p99_latency_ms; result_path: benchmarks/results/payments-baseline.json.
- [x] SDD and OpenSpec artifacts agree with the implementation.
- [x] Domain logic is isolated from transport, persistence, broker, provider, and vendor details.
- [x] SOLID, DRY, KISS, YAGNI, and Law of Demeter review has no unexplained exception.
- [x] Tests cover the contract and the failure paths that affect the claim.
      Evidence (2026-07-27): `gradle clean test writeCoverage bootJar` ran inside the
      Docker build stage and passed; core coverage enforced at 95.65%.
- [x] Docker runs the documented default path from a clean checkout.
      Evidence (2026-07-27): `docker build -t spring-hexagonal-payments .` exit 0
      (597MB image); `docker run --rm -e DURATION=2s -e VUS=8 spring-hexagonal-payments`
      completed the ephemeral PostgreSQL + app + k6 demo with checks_rate 1.0 and
      http_failure_rate 0.0.
- [x] CI runs the same meaningful checks without mutable dependencies or secrets.
      Evidence (2026-07-27): `ci.yml` pins actions/checkout@d23441a (v6.1.0),
      actions/setup-java@03ad4de (v5.6.0), gradle/actions/setup-gradle@3f131e8 (v6);
      all SHAs verified against upstream tag refs via the GitHub API. Workflow
      declares `permissions: contents: read` and `timeout-minutes: 25`; no secrets.
- [x] Benchmark writes valid JSON under `benchmarks/results/` and can be repeated.
      payments-baseline.json and payments-confirmation.json present; smoke run
      reproduced valid JSON (p99 28.404 ms at reduced 2s/8VU smoke load).
- [x] README, benchmark JSON, and `project.yaml` report the same primary metric.
      All three report p99_latency_ms with baseline value 131.414 ms.
- [x] Reuse review records every kit improvement, backlog item, or rejected duplication.
- [x] Independent review found no blocker; CI green on clean checkout.
      Evidence (2026-07-27): run 30293761301 concluded success (build + tests +
      coverage + Docker demo smoke) on branch codex/spring-hexagonal-payments/publication-gates.
