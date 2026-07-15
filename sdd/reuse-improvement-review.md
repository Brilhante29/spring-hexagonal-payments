# Reuse Improvement Review

Project: `11 - spring-hexagonal-payments`

## Review Points

- [x] after scaffold
- [x] after architecture decision
- [x] after first working slice
- [x] after benchmark result
- [x] before publication
- [ ] after CI failure, if applicable

## Findings

| Finding | Classification | Kit area | Action | Status |
|---|---|---|---|---|
| Any `src/` directory was treated as Python | `patch_now` | validation | Detect Python source files, not a generic folder | patched in kit `5de78b6` |
| Gradle repositories could pass without a portable wrapper | `patch_now` | validation | Require both wrapper scripts, JAR, properties, and run `check` when Java exists | patched in kit `5de78b6` |
| Spring Boot 4 modularized Flyway and defaults to Jackson 3 | `patch_now` | profiles/skills | Record starter and JSON migration rules in the Spring Kotlin profile | patched in kit `cffc3de` |
| k6 custom summaries omit p99 unless requested | `patch_now` | skills/harness | Require `summaryTrendStats` to include p99 | patched in kit `cffc3de` |
| Setup traffic can inflate reported throughput | `patch_now` | skills/harness | Separate warm-up metrics and divide measured iterations by measured duration | patched in kit `cffc3de` |
| Sandbox staging can overwrite a newer project file | `reject` | tooling | Keep environment-specific copy mechanics outside the public kit; compare files before copy | mitigated locally |

## OpenSpec Self-Review

- Is hexagonal architecture solving a real force? Yes: transaction and persistence substitution are material to the invariant.
- Is the framework leaking inward? No; the architecture test rejects those imports.
- Is the benchmark measuring the claim? Yes; it measures successful authorization after unmeasured warm-up and enforces failures/checks.
- Is another distributed component needed? No; there is no asynchronous or independent deployment requirement.
- Is a project lesson reusable? Yes; Spring Boot 4 migration and k6 measurement rules belong in the kit.

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects repeated mistakes discovered during the project.
- [x] Project was synced from final kit commit `cffc3de`.
- [ ] Public CI is green on the published project commit.
