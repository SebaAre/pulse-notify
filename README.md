# PulseNotify

An event-driven, multi-channel notification platform built on a cloud-native microservices architecture.

## Architecture Overview

```
                          ┌─────────────────────────────────────────────────────────────┐
                          │                        AWS Cloud                             │
  Clients                 │                                                              │
  ───────                 │  ┌──────────────┐    ┌───────────────────────────────────┐  │
  Web App ──────────────► │  │ API Gateway  │───►│         gateway-service           │  │
  Mobile  ──────────────► │  │  (AWS)       │    │  (Spring Cloud Gateway + Auth)    │  │
  3rd Party ────────────► │  └──────────────┘    └──────────────┬────────────────────┘  │
                          │                                      │                       │
                          │         ┌────────────────────────────┼──────────────────┐   │
                          │         ▼                            ▼                  ▼   │
                          │  ┌─────────────┐    ┌──────────────────┐  ┌───────────────┐│
                          │  │   user-     │    │  notification-   │  │  template-    ││
                          │  │   service   │    │    service       │  │   service     ││
                          │  └──────┬──────┘    └────────┬─────────┘  └───────────────┘│
                          │         │                     │                             │
                          │         └──────────┬──────────┘                            │
                          │                    ▼                                        │
                          │         ┌──────────────────┐                               │
                          │         │   Apache Kafka    │                               │
                          │         │  (MSK on AWS)    │                               │
                          │         └──────────┬───────┘                               │
                          │                    │                                        │
                          │         ┌──────────┴──────────┐                            │
                          │         ▼                      ▼                            │
                          │  ┌─────────────┐    ┌──────────────────┐                  │
                          │  │  delivery-  │    │  audit-service   │                  │
                          │  │  service    │    │  (DynamoDB)      │                  │
                          │  └──────┬──────┘    └──────────────────┘                  │
                          │         │                                                   │
                          │    ┌────┴─────────────────┐                                │
                          │    ▼          ▼            ▼                                │
                          │  [SNS]      [SES]        [SQS]                             │
                          │  (Push)     (Email)      (Webhook)                          │
                          └─────────────────────────────────────────────────────────────┘
```

## Services

| Service              | Responsibility                              | Port | Database   |
|----------------------|---------------------------------------------|------|------------|
| `gateway-service`    | API Gateway, JWT auth, rate limiting        | 8080 | —          |
| `notification-service` | Notification orchestration & routing      | 8081 | PostgreSQL |
| `delivery-service`   | Multi-channel delivery via SNS/SES/SQS     | 8082 | —          |
| `template-service`   | Template CRUD & rendering                  | 8083 | PostgreSQL |
| `user-service`       | User preferences & channel subscriptions   | 8084 | PostgreSQL |
| `audit-service`      | Immutable event audit trail                | 8085 | DynamoDB   |

## Kafka Topics

| Topic                       | Producer               | Consumer(s)                      |
|-----------------------------|------------------------|----------------------------------|
| `notification.requested`    | notification-service   | delivery-service, audit-service  |
| `notification.processed`    | notification-service   | audit-service                    |
| `delivery.attempted`        | delivery-service       | audit-service                    |
| `delivery.completed`        | delivery-service       | notification-service, audit-service |
| `delivery.failed`           | delivery-service       | notification-service, audit-service |

## Quick Start

### Prerequisites
- Java 21, Maven 3.9+
- Node.js 20+, npm 10+
- Docker & Docker Compose
- AWS CLI (for cloud deployment)

### Start Local Infrastructure

```bash
make infra-up         # Start Kafka, PostgreSQL, DynamoDB Local, observability stack
make kafka-topics     # Create required Kafka topics
```

### Build & Run Services

```bash
make build            # Build all Java services
make test             # Run all tests

# Run individual services
cd services/notification-service && mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev           # Dev server on http://localhost:5173
```

### Observability

| Tool        | URL                    | Credentials      |
|-------------|------------------------|------------------|
| Grafana     | http://localhost:3000  | admin / admin    |
| Prometheus  | http://localhost:9090  | —                |
| Loki        | http://localhost:3100  | —                |

## Project Layout

```
pulse-notify/
├── services/           # Spring Boot microservices
├── shared/             # Shared Maven modules (events, common)
├── frontend/           # React 18 + Vite + TypeScript
├── infra/
│   ├── terraform/      # AWS infrastructure (EKS, RDS, MSK, DynamoDB)
│   ├── k8s/            # Kubernetes manifests
│   ├── kafka/          # Topic definitions
│   └── docker/         # Supporting Docker configs
├── observability/      # Prometheus, Loki, Grafana configs
├── scripts/            # Local dev & deployment scripts
└── .github/workflows/  # CI/CD pipelines
```

## Deployment

Infrastructure is managed with Terraform targeting AWS. Services run on EKS.

```bash
cd infra/terraform
terraform init
terraform plan -var-file=environments/prod.tfvars
terraform apply
```

See `infra/terraform/README.md` for detailed AWS setup.
