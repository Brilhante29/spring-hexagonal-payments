# References

No source code was copied from the references below. Domain behavior, SQL, tests, fixtures, Docker orchestration, and benchmark code are project-specific.

| Reference | License | Used for | Copied code? |
|---|---|---|---|
| [Spring Boot](https://spring.io/projects/spring-boot/) | Apache-2.0 | Supported Spring Boot line and production conventions | no |
| [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html) | Apache-2.0 documentation | Java and Gradle compatibility | no |
| [Spring Framework Kotlin requirements](https://docs.spring.io/spring-framework/reference/languages/kotlin/requirements.html) | Apache-2.0 documentation | Kotlin, reflection, and Jackson requirements | no |
| [Spring Boot database initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html) | Apache-2.0 documentation | Flyway starter and migration lifecycle | no |
| [Spring Boot JSON](https://docs.spring.io/spring-boot/4.0/reference/features/json.html) | Apache-2.0 documentation | Jackson 3 default and Kotlin module choice | no |
| [Jackson module Kotlin](https://github.com/FasterXML/jackson-module-kotlin) | Apache-2.0 | Kotlin serialization with Jackson 3 | no |
| [Kotlin releases](https://kotlinlang.org/docs/releases.html) | Apache-2.0 | Kotlin 2.4.10 selection | no |
| [Gradle 9.3 release notes](https://docs.gradle.org/9.3.0/release-notes.html) | Apache-2.0 | Build tool and wrapper selection | no |
| [Gradle wrapper verification](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:verification) | Apache-2.0 documentation | Distribution checksum verification | no |
| [PostgreSQL 18 documentation](https://www.postgresql.org/docs/18/) | PostgreSQL License | Schema, locking, and conflict semantics | no |
| [Testcontainers PostgreSQL module](https://java.testcontainers.org/modules/databases/postgres/) | MIT | Real PostgreSQL integration-test lifecycle | no |
| [Grafana k6 options](https://grafana.com/docs/k6/latest/using-k6/k6-options/reference/) | AGPL-3.0 for k6 | p99 summary statistics and load configuration | no |
| [Grafana k6 thresholds](https://grafana.com/docs/k6/latest/using-k6/thresholds/) | AGPL-3.0 for k6 | Executable latency and failure gates | no |
| [thombergs/buckpal](https://github.com/thombergs/buckpal) | No license relied upon | Hexagonal package organization and boundary-testing reference | no |
| [Rocketseat 05-nest-clean](https://github.com/rocketseat-education/05-nest-clean) | No license relied upon | Clean architecture repository organization reference | no |
| [programadorLhama](https://github.com/programadorLhama) | Profile; repository-specific licenses apply | Repository presentation and organization reference | no |
| [Paulescu](https://github.com/Paulescu) | Profile; repository-specific licenses apply | Portfolio narrative and measurable ML project organization reference | no |

## Reuse Boundary

- Reused: official libraries under their licenses, architectural vocabulary, repository organization ideas, and benchmark conventions.
- Project-owned: payment model, ports, service behavior, HTTP contract, SQL migration, adapters, tests, one-container demo, k6 workload, and result JSON.
- Not imported: source files, fixtures, domain rules, branding, or README text from the organizational references.
