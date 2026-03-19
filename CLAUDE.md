# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build everything (from repo root)
./mvnw clean package -DskipTests

# Build a single service
./mvnw clean package -DskipTests -pl services/notification-service -am

# Run all tests
./mvnw verify

# Test a single service
./mvnw test -pl services/notification-service

# Lint (Checkstyle)
./mvnw checkstyle:check

# Frontend
cd frontend && npm install
npm run dev          # dev server :5173, proxies /api → gateway :8080
npm run build
npm run test
npm run lint

# Local infrastructure (Kafka, Postgres, DynamoDB, observability)
make infra-up
make kafka-topics    # run once after infra-up

# Run individual service (from service directory)
./mvnw spring-boot:run
```

## Architecture

PulseNotify is a Maven multi-module mono-repo. The root `pom.xml` is the parent for all Java modules. Services are independently deployable Spring Boot applications that share two library modules.

### Module Layout

```
pulse-notify/
├── shared/
│   ├── events/       # Kafka event POJOs shared by all services (no Spring Boot plugin)
│   └── common/       # Cross-cutting: error handling, pagination, response wrappers
├── services/
│   ├── gateway-service     :8080  Spring Cloud Gateway — reactive, JWT validation, rate limiting
│   ├── notification-service :8081  Core orchestration, PostgreSQL, Kafka producer
│   ├── delivery-service    :8082  Kafka consumer → SNS/SES/SQS, no database
│   ├── template-service    :8083  Template CRUD + Thymeleaf rendering, PostgreSQL
│   ├── user-service        :8084  User preferences & subscriptions, PostgreSQL
│   └── audit-service       :8085  Kafka consumer → DynamoDB, append-only
├── frontend/               React 18 + Vite + TypeScript, path alias @/ = src/
├── infra/
│   ├── terraform/          AWS infrastructure (EKS, RDS, MSK, DynamoDB, SQS/SNS, S3)
│   ├── k8s/                Kubernetes manifests (namespace, ingress, per-service deployment/service/hpa)
│   ├── kafka/topics.yaml   Canonical topic definitions
│   └── docker/postgres/    init.sql creates schemas for local dev
├── observability/          Prometheus scrape config, Loki, Grafana provisioning
└── scripts/                setup-local.sh, kafka-topics.sh, deploy.sh
```

### Event Flow

```
Client → API Gateway (AWS) → gateway-service
  → notification-service  ──publishes──► notification.requested (Kafka)
       ↑ delivery.completed/failed              │
       │                                        ▼
       │                               delivery-service
       │                               (routes to SNS/SES/SQS)
       │                               ──publishes──► delivery.attempted
       │                                            ► delivery.completed / delivery.failed
       │
       └── audit-service (subscribes to ALL topics → DynamoDB)
```

### Service Internals

Each service follows the same internal package structure:
```
com.pulsenotify.<service>/
  config/       Spring @Configuration classes (Kafka, AWS, Security)
  controller/   REST controllers (@RestController)
  service/      Business logic (@Service)
  repository/   Spring Data JPA or DynamoDB Enhanced Client
  model/        JPA @Entity or DynamoDB @DynamoDbBean classes
  dto/          Request/response POJOs (never expose entities directly)
  event/        Kafka @KafkaListener consumers and KafkaTemplate producers
  exception/    Custom exceptions and @ControllerAdvice handler
```

`shared/events` classes are the canonical Kafka message contracts — imported as a dependency, never duplicated.

### Databases

| Service              | Storage        | Schema/Table           |
|----------------------|----------------|------------------------|
| notification-service | PostgreSQL     | `notification` schema  |
| template-service     | PostgreSQL     | `template` schema      |
| user-service         | PostgreSQL     | `user` schema          |
| audit-service        | DynamoDB       | `pulse-audit-events`   |

Flyway manages PostgreSQL migrations. Migration files live in each service at `src/main/resources/db/migration/`. In local dev, all three PostgreSQL-backed services share one container; schemas are isolated.

`audit-service` uses the AWS SDK v2 DynamoDB Enhanced Client (`software.amazon.awssdk:dynamodb-enhanced`). For local dev the endpoint is overridden to `http://localhost:8000` (DynamoDB Local).

### AWS Integration

`delivery-service` uses AWS SDK v2 directly (no Spring Cloud AWS). Clients for SNS, SES, and SQS are configured as Spring beans in `config/AwsConfig.java`. The `aws.endpoint-override` property in `application.yml` redirects to LocalStack during local development.

In CI, Testcontainers Localstack (`org.testcontainers:localstack`) provides a real LocalStack container for integration tests.

### gateway-service

Uses Spring Cloud Gateway (reactive / WebFlux — **not** servlet-based). JWT validation is done via Spring Security OAuth2 Resource Server. Route definitions go in `config/GatewayConfig.java`, not in `application.yml`.

### Frontend

- Path alias `@/` maps to `src/` (configured in both `vite.config.ts` and `tsconfig.json`).
- API calls go through `/api` prefix, which Vite proxies to `http://localhost:8080` in dev.
- State management: Zustand for global state, TanStack Query for server state.
- Tests use Vitest + Testing Library. Test setup file: `src/test/setup.ts`.

### Observability

Every service exposes `/actuator/prometheus` (port same as service). Prometheus scrapes all services via `observability/prometheus/prometheus.yml` using `host.docker.internal` for local dev. Loki ingests logs via Promtail. Grafana dashboards are provisioned from `observability/grafana/dashboards/` (JSON files, auto-loaded).

### Kafka Topics

Canonical definitions: `infra/kafka/topics.yaml`. Create locally via `make kafka-topics` (runs `scripts/kafka-topics.sh`). Default local replication factor is 1; set `REPLICATION_FACTOR=3` when targeting MSK.

Dead-letter topic: `delivery.failed.dlq` — consumed by `delivery-service` for retry logic.

## Testing Conventions

- Unit tests: `@ExtendWith(MockitoExtension.class)`, no Spring context.
- Controller tests: `@WebMvcTest` + MockMvc (servlet services) or `@WebFluxTest` + WebTestClient (gateway-service).
- Integration tests: `@SpringBootTest` + Testcontainers. Tag them with `@Tag("integration")` to run separately via `mvn test -Dgroups=integration`.
- Kafka integration tests use embedded Kafka via `spring-kafka-test` (`@EmbeddedKafka`).

## Docker Images

`Dockerfile` in each service directory expects the JAR already built by Maven. Build sequence:
```bash
./mvnw clean package -DskipTests
docker build -t pulsenotify/notification-service:latest \
  -f services/notification-service/Dockerfile \
  services/notification-service
```

`make docker-build` builds all service images.

## Deployment

EKS manifests are in `infra/k8s/<service-name>/`. Each service has `deployment.yaml`, `service.yaml`, and `hpa.yaml`. The `infra/k8s/ingress.yaml` routes `api.pulsenotify.io` → gateway-service and `app.pulsenotify.io` → frontend via AWS ALB Ingress Controller.

`scripts/deploy.sh <staging|prod> <image-tag>` performs a `kubectl set image` rolling update for all services.
