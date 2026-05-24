# PulseNotify

An event-driven, multi-channel notification platform built on a cloud-native microservices architecture.

**Status:** runs end-to-end locally via Docker Compose. Kubernetes manifests are included for cluster deployment — not currently deployed to a managed cluster.

**Stack at a glance:** Java 21 · Spring Boot 3.2 · Apache Kafka · PostgreSQL · DynamoDB · AWS SDK v2 (SES/SNS/SQS) · React 18 + TypeScript · Prometheus/Loki/Grafana · Kubernetes.

## Architecture Overview

```
  Clients
  ───────
  Web App  ──┐
  Mobile   ──┼──► AWS API Gateway ──► gateway-service :8080
  3rd Party ─┘                        (Spring Cloud Gateway + JWT)
                                                │
                          ┌─────────────────────┼─────────────────────┐
                          ▼                     ▼                     ▼
                   user-service          notification-service   template-service
                     :8084                    :8081                 :8083
                   (PostgreSQL)            (PostgreSQL)           (PostgreSQL)
                                                │
                                                │ publishes
                                                ▼
                                       notification.requested
                                                │
                                    ┌───────────┴───────────┐
                                    ▼                       ▼
                            delivery-service          audit-service
                               :8082                     :8085
                          (SNS / SES / SQS)            (DynamoDB)
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
                  [SES]           [SNS]           [SQS]
                 (EMAIL)          (SMS)           (PUSH)
```

## Event Flow

```
notification-service  ──► notification.requested  ──► delivery-service
                                                            │
                                                 ┌──────────┼──────────┐
                                                 ▼          ▼          ▼
                                          delivery.    delivery.   delivery.
                                          attempted   completed    failed
                                                 └──────────┼──────────┘
                                                            ▼
                                                      audit-service
                                                       (DynamoDB)
```

## Services

| Service                | Responsibility                              | Port | Database   |
|------------------------|---------------------------------------------|------|------------|
| `gateway-service`      | API Gateway, JWT auth, rate limiting        | 8080 | —          |
| `notification-service` | Notification orchestration & routing        | 8081 | PostgreSQL |
| `delivery-service`     | Multi-channel delivery via SNS/SES/SQS      | 8082 | —          |
| `template-service`     | Template CRUD & Freemarker rendering        | 8083 | PostgreSQL |
| `user-service`         | User preferences & channel subscriptions    | 8084 | PostgreSQL |
| `audit-service`        | Immutable event audit trail                 | 8085 | DynamoDB   |

## Kafka Topics

| Topic                    | Producer             | Consumer(s)                          |
|--------------------------|----------------------|--------------------------------------|
| `notification.requested` | notification-service | delivery-service, audit-service      |
| `delivery.attempted`     | delivery-service     | audit-service                        |
| `delivery.completed`     | delivery-service     | notification-service, audit-service  |
| `delivery.failed`        | delivery-service     | notification-service, audit-service  |
| `delivery.failed.dlq`    | delivery-service     | delivery-service (retry, max 3)      |

## Tech Stack

| Layer          | Technology                                      |
|----------------|-------------------------------------------------|
| Language       | Java 21                                         |
| Framework      | Spring Boot 3.2.3, Spring Cloud 2023.0.0        |
| Messaging      | Apache Kafka (MSK on AWS)                       |
| Databases      | PostgreSQL (JPA + Flyway), DynamoDB (AWS SDK v2)|
| AWS Services   | SES (email), SNS (SMS), SQS (push/webhook)      |
| Gateway        | Spring Cloud Gateway (reactive / WebFlux)       |
| Templates      | Freemarker                                      |
| Frontend       | React 18, Vite, TypeScript, TanStack Query      |
| Observability  | Prometheus, Loki, Grafana                       |
| Deployment     | Kubernetes (Deployment, Service, HPA, Ingress)  |

## delivery-service Design

The delivery-service uses the **Strategy Pattern** to route notifications to the correct AWS channel without a hard-coded switch statement. Each channel is an independent `@Component` that implements `DeliveryHandler`:

```
DeliveryHandler (interface)
├── EmailDeliveryHandler  →  AWS SES
├── SmsDeliveryHandler    →  AWS SNS
└── PushDeliveryHandler   →  AWS SQS
```

`DeliveryService` receives all handlers as a `List<DeliveryHandler>` injected by Spring, selects the matching handler via `supports()`, and delegates. Adding a new channel (e.g. WhatsApp) requires only a new `@Component` — no changes to `DeliveryService`.

## Quick Start

### Prerequisites

- Java 21, Maven 3.9+
- Node.js 20+, npm 10+
- Docker & Docker Compose
- AWS CLI (for cloud deployment)

### Start Local Infrastructure

```bash
make infra-up        # Kafka, PostgreSQL, DynamoDB Local, LocalStack, observability stack
make kafka-topics    # Create required Kafka topics (run once after infra-up)
```

### Build & Run Services

```bash
make build           # Build all Java services (skips tests)
make test            # Run all tests

# Run individual service
cd services/notification-service && ./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev          # Dev server on http://localhost:5173 (proxies /api → :8080)
```

The web app (React 18 + Vite + TanStack Query) ships five pages, one per backend domain:

| Route            | Page              | Hits service(s)                 |
|------------------|-------------------|---------------------------------|
| `/`              | Dashboard         | notification-service `/stats`   |
| `/notifications` | Notifications     | notification-service            |
| `/templates`     | Templates         | template-service                |
| `/users`         | Users             | user-service                    |
| `/audit`         | Audit log         | audit-service                   |

All calls go through `/api/*`, proxied to the gateway in dev and routed by Spring Cloud Gateway to the right downstream service.

### Observability

| Tool       | URL                   | Credentials |
|------------|-----------------------|-------------|
| Grafana    | http://localhost:3000 | see `.env`  |
| Prometheus | http://localhost:9090 | —           |
| Loki       | http://localhost:3100 | —           |

## Code Highlights

A few things in the codebase that I think are worth a closer look:

- **Strategy pattern in delivery-service** — `DeliveryService` accepts a `List<DeliveryHandler>` injected by Spring and picks the matching handler via `supports()`. Adding a new channel is a single new `@Component` with zero edits to existing code. See `services/delivery-service/src/main/java/com/pulsenotify/delivery/service/`.
- **Closed feedback loop on Kafka** — `delivery-service` publishes `delivery.completed` / `delivery.failed`; `notification-service` consumes them and transitions the persisted notification row from `PENDING` → `SENT` / `FAILED`. See `notification-service/.../event/DeliveryEventConsumer.java` and `service/NotificationService#markStatus`.
- **DLQ with bounded retry** — permanent failures in `delivery-service` are published to `delivery.failed.dlq` carrying the original `NotificationRequestedEvent` plus an `x-delivery-attempt` Kafka header. `DlqRetryConsumer` re-attempts up to 3 times and then drops, avoiding poison-message loops. See `services/delivery-service/.../event/DlqRetryConsumer.java`.
- **End-to-end integration test with real infrastructure** — `NotificationFlowIntegrationTest` spins up Postgres and Kafka via Testcontainers, hits the controller over HTTP, publishes a delivery event on Kafka, and uses Awaitility to verify the row transitions to `SENT`. No mocks. Tagged `@Tag("integration")` and excluded from the default Surefire run; opt in with `make test-integration SVC=notification-service`.
- **Multi-storage by intent** — PostgreSQL for transactional domains (notifications, templates, users) with Flyway migrations; DynamoDB for the append-only audit trail via the AWS SDK v2 Enhanced Client. Schemas are isolated per service even though they share a Postgres container locally.
- **Shared event contracts** — Kafka message POJOs live in `shared/events` and are imported as a Maven dependency. There is one canonical class per event type; services never duplicate the shape.
- **Reactive gateway, servlet downstreams** — `gateway-service` is WebFlux + Spring Cloud Gateway; downstream services are servlet-based Spring MVC. JWT validation and rate limiting are gateway-side; downstreams trust the routed traffic.
- **Atomic commit history** — one modified file per commit, past-tense subjects. Browse `git log --oneline` to walk the build feature by feature.

## Project Layout

```
pulse-notify/
├── shared/
│   ├── events/         # Kafka event POJOs — shared contracts across all services
│   └── common/         # Cross-cutting: error handling, pagination, response wrappers
├── services/
│   ├── gateway-service      :8080
│   ├── notification-service :8081
│   ├── delivery-service     :8082
│   ├── template-service     :8083
│   ├── user-service         :8084
│   └── audit-service        :8085
├── frontend/           # React 18 + Vite + TypeScript
├── infra/
│   ├── k8s/            # Kubernetes manifests (Deployment, Service, HPA per service)
│   ├── kafka/          # Topic definitions
│   └── docker/         # Supporting Docker configs
├── observability/      # Prometheus scrape config, Loki, Grafana dashboards
├── scripts/            # setup-local.sh, kafka-topics.sh, deploy.sh
└── .github/workflows/  # CI/CD pipelines
```

## Kubernetes Deployment

> The project runs entirely on local Docker today. The Kubernetes manifests below describe the deployment topology and are ready to apply to any conformant cluster (kind, k3d, EKS, GKE, AKS) — the project is not actively running in a managed cluster.

Layout under `infra/k8s/`:

```
infra/k8s/
├── namespace.yaml                # pulse-notify namespace
├── configmap.yaml                # shared service-discovery config (Kafka, DB host, etc.)
├── secrets-example.yaml          # placeholder Secret manifests — do not apply as-is
├── ingress.yaml                  # ALB Ingress: api.pulsenotify.io → gateway, app.pulsenotify.io → frontend
├── gateway-service/              # Deployment, Service, HPA
├── notification-service/         # Deployment, Service, HPA
├── delivery-service/             # Deployment, Service, HPA
├── template-service/             # Deployment, Service, HPA
├── user-service/                 # Deployment, Service, HPA
├── audit-service/                # Deployment, Service, HPA
└── frontend/                     # nginx Deployment, Service, HPA
```

Each service: ConfigMap envFrom for shared config, individual `secretKeyRef` for credentials, liveness/readiness probes on `/actuator/health/*`, prometheus scrape annotations, and an HPA tuned per service profile (gateway 2-12, delivery 2-15 for burst, internals 2-6).

Bootstrap a cluster:

```bash
kubectl apply -f infra/k8s/namespace.yaml
kubectl apply -f infra/k8s/configmap.yaml
# create real Secrets (see secrets-example.yaml header for instructions)
kubectl apply -R -f infra/k8s/
```

Rolling deploys via image tags:

```bash
scripts/deploy.sh <staging|prod> <image-tag>
```
