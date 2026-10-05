# HealthPay — Healthcare Claims, Billing and Payment Platform

> This application uses synthetic data and is an educational portfolio project.
> It is not a production healthcare or payment processing system.
> It is **not** HIPAA compliant or PCI compliant. It applies security practices that are relevant in those environments.

HealthPay is a backend system that combines **healthcare software engineering** (patients, providers, insurance coverage, claims, FHIR R4) with **fintech backend engineering** (invoices, payments, idempotency, a double-entry ledger, refunds, event-driven processing).

It is built as a **modular monolith** with Java and Spring Boot, one phase at a time. See [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) for the current status.

## Current status

**Phase 1 — Spring Boot foundation.** The application starts, connects to PostgreSQL, Redis and Kafka, runs Flyway migrations, protects all endpoints except public health and documentation endpoints, and exposes `GET /api/health`.

Business features (users, patients, claims, payments, ledger, ...) are added in later phases.

## Technology stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Spring Framework 7, Spring Security 7) |
| Build | Maven (via Maven Wrapper) |
| Database | PostgreSQL 17, Spring Data JPA (Hibernate), Flyway |
| Cache / rate limiting | Redis 7.4 |
| Messaging | Apache Kafka 4.2 (KRaft mode) |
| API docs | springdoc-openapi (Swagger UI) |
| Operations | Spring Boot Actuator |
| Testing | JUnit, Spring Boot Test, Testcontainers |
| Local environment | Docker, Docker Compose |

Planned later: JWT authentication, HAPI FHIR (R4), GitHub Actions CI, AWS deployment.

## Local development requirements

- Docker with the Docker Compose plugin (`docker compose version`)
- Git, curl (jq is useful for reading JSON)
- Optional: JDK 21, only if you want to run `./mvnw` directly

Maven never needs to be installed. `./mvnw` downloads the correct version.

## Quick start

### Option A — Docker only (no Java needed)

```bash
cp .env.example .env                          # local settings (git-ignored)
./scripts/docker-mvn.sh clean verify          # build + unit + integration tests inside Docker
docker compose --profile app up -d --build    # PostgreSQL, Redis, Kafka and the app
docker compose logs -f app                    # wait for "Started HealthPayApplication"
docker compose --profile app down
```

### Option B — with JDK 21 installed

```bash
cp .env.example .env          # local settings (git-ignored)
docker compose up -d          # PostgreSQL, Redis, Kafka
docker compose ps             # wait until all three are (healthy)
./mvnw clean verify           # unit + integration tests (Docker must be running)
./mvnw spring-boot:run        # start the app on http://localhost:8080
```

Check it:

```bash
curl http://localhost:8080/api/health
# {"status":"UP"}
```

## Useful endpoints

| Endpoint | Access | Purpose |
|---|---|---|
| `GET /api/health` | public | Liveness: the app is running |
| `GET /actuator/health` | public | Dependency health: database and Redis |
| `GET /swagger-ui.html` | public | Interactive API documentation |
| `GET /v3/api-docs` | public | OpenAPI document (JSON) |
| anything else | protected | Returns `401` until authentication is added (Phase 2) |

## Testing

```bash
./mvnw test      # fast tests only (*Test), no Docker needed
./mvnw verify    # fast tests + integration tests (*IT) using Testcontainers

# Same thing without Java installed:
./scripts/docker-mvn.sh test
./scripts/docker-mvn.sh verify
```

Integration tests start their own PostgreSQL, Redis and Kafka containers. They do not use the Docker Compose containers.

## Configuration

All environment-specific settings come from environment variables. For local development they are read from `.env` (copied from `.env.example`). `.env` is never committed. Real environment variables override `.env`.

## Project layout

```
src/main/java/com/healthpay
  HealthPayApplication.java
  common/
    config/    SecurityConfig, OpenApiConfig
    health/    HealthController, HealthResponse
src/main/resources
  application.yml
  db/migration/   Flyway SQL migrations (V1__..., V2__..., ...)
src/test/java/com/healthpay
  support/        Testcontainers setup and integration test base class
scripts/
  docker-mvn.sh   run any Maven command inside a Java 21 container
Dockerfile        multi-stage build: JDK to build, JRE to run, non-root user
docker-compose.yml
```

Domain modules (`auth`, `patient`, `provider`, `insurance`, `claim`, `billing`, `payment`, `ledger`, `fhir`, `audit`, `event`) are added in their phases.
