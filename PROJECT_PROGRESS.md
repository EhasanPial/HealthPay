# HealthPay — Project Progress

## Phase status

| Phase | Name | Status |
|---|---|---|
| 0 | Project planning | Partly done: README.md and this file exist. ARCHITECTURE.md and the entity/relationship design are still to do. |
| 1 | Spring Boot foundation | Implemented. Waiting for first local `clean verify` to pass. |
| 2 | Authentication and users | Not started |
| 3 | Patient and provider management | Not started |
| 4 | Insurance coverage | Not started |
| 5 | Claims system | Not started |
| 6 | FHIR support | Not started |
| 7 | Billing and invoices | Not started |
| 8 | Payment processing | Not started |
| 9 | Double-entry ledger | Not started |
| 10 | Refunds | Not started |
| 11 | Kafka and transactional outbox | Not started |
| 12 | Redis caching and rate limiting | Not started |
| 13 | Audit logging | Not started |
| 14 | Error handling and API quality | Not started |
| 15 | Search, filtering, sorting, pagination review | Not started |
| 16 | Demo data and shell acceptance tests | Not started |
| 17 | Automated testing pyramid | Not started |
| 18 | Concurrency and reliability | Not started |
| 19 | Observability | Not started |
| 20 | Docker and local environment | Started early in Phase 1: basic Dockerfile, app service in Compose, `scripts/docker-mvn.sh`. To polish later. |
| 21 | GitHub Actions CI | Not started |
| 22 | AWS deployment | Not started |
| 23 | Optional ML fraud scoring service | Not started |
| 24 | Final README | Not started |
| 25 | Resume preparation | Not started |

---

## Phase 1 — Spring Boot foundation

### What was built

- Spring Boot 4.1.1 application on Java 21, built with the Maven Wrapper.
- Dependencies for web, validation, JPA, Flyway, Spring Security, Redis, Kafka, Actuator and springdoc-openapi.
- `docker-compose.yml` with PostgreSQL 17, Redis 7.4 and Kafka 4.2 (KRaft), each with a healthcheck.
- `GET /api/health` returning `{"status":"UP"}`.
- Security baseline: health, API docs and `/error` are public; everything else returns `401`.
- First Flyway migration `V1__baseline.sql`.
- Docker-only workflow (no local Java needed):
  - `Dockerfile` — multi-stage build (JDK 21 to build, JRE 21 to run), runs as a non-root user.
  - `docker-compose.yml` — `app` service under the `app` profile.
  - `scripts/docker-mvn.sh` — runs any Maven command in a Java 21 container, with Docker access for Testcontainers.
  - `.dockerignore` — keeps `.env`, `target/` and `.git/` out of the image.
- Tests:
  - `HealthControllerTest` — web-layer test, no Docker.
  - `HealthApiIT` — full app over real HTTP with Testcontainers.
  - `InfrastructureIT` — checks Flyway ran, Redis read/write works, Kafka broker is reachable.

### Decisions

| Decision | Reason |
|---|---|
| Spring Boot 4.1 instead of 3.5 | Spring Boot 3.5 open-source support ended on 2026-06-30. A new project should start on a supported line. |
| Java 21 | Required by the project plan; fully supported by Spring Boot 4. |
| Flyway owns the schema, Hibernate `ddl-auto: validate` | Schema changes are versioned, reviewed SQL. Hibernate never changes tables by itself. |
| `spring.jpa.open-in-view: false` | Database access stays in the service layer and inside transactions. |
| `/api/health` (liveness) and `/actuator/health` (dependencies) | A process check must stay fast and must not fail because a dependency is down. |
| Testcontainers with `@ServiceConnection` | Tests run against real PostgreSQL/Redis/Kafka instead of in-memory fakes that behave differently. |
| Unit tests `*Test` (Surefire), integration tests `*IT` (Failsafe) | `./mvnw test` stays fast and Docker-free; `./mvnw verify` runs everything. |
| Settings from environment variables, local values in git-ignored `.env` | No secrets in the repository. The same code runs locally and on AWS. |
| Stateless security, CSRF disabled | The API will use bearer tokens (JWT), not cookie sessions, so CSRF does not apply. |
| Multi-stage Dockerfile, non-root user | Small runtime image without source code or build tools; less damage possible if the app is ever compromised. |
| App service behind a Compose profile | `docker compose up -d` still starts only dependencies (for `./mvnw` users); `--profile app` adds the app. |
| `.env` excluded from the Docker image | Secrets are passed at run time as environment variables, never baked into an image. |

### Indexes added

None yet. There are no business tables in this phase.

### How to verify

Docker only:

```bash
cp .env.example .env
./scripts/docker-mvn.sh clean verify
docker compose --profile app up -d --build
curl http://localhost:8080/api/health
```

With JDK 21 installed:

```bash
cp .env.example .env
docker compose up -d
./mvnw clean verify
./mvnw spring-boot:run
curl http://localhost:8080/api/health
```

### Known limitations

- No authentication yet. Protected endpoints return an empty `401` body; a consistent JSON error format comes in Phase 14.
- Spring Boot has no built-in Kafka health indicator, so Kafka is checked by `InfrastructureIT`, not by `/actuator/health`.

### Next

Phase 2 — Authentication and users (User, Role, registration, login, password hashing, JWT, role-based access).
