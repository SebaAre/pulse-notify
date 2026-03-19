.PHONY: help build test lint clean infra-up infra-down kafka-topics \
        frontend-install frontend-dev frontend-build deploy-staging deploy-prod

MAVEN  := ./mvnw
NPM    := npm
DC     := docker compose
AWS    := aws

## ── Help ─────────────────────────────────────────────────────────────────────
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	  awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2}'

## ── Java Build ───────────────────────────────────────────────────────────────
build: ## Build all Java services (skip tests)
	$(MAVEN) clean package -DskipTests

build-service: ## Build a single service: make build-service SVC=notification-service
	$(MAVEN) clean package -DskipTests -pl services/$(SVC) -am

test: ## Run all tests
	$(MAVEN) verify

test-service: ## Test a single service: make test-service SVC=notification-service
	$(MAVEN) test -pl services/$(SVC)

test-unit: ## Run unit tests only (excludes integration tests)
	$(MAVEN) test -pl services/$(SVC) -Dgroups="unit"

test-integration: ## Run integration tests only
	$(MAVEN) verify -pl services/$(SVC) -Dgroups="integration"

lint: ## Run Checkstyle across all modules
	$(MAVEN) checkstyle:check

clean: ## Clean all build artifacts
	$(MAVEN) clean

## ── Local Infrastructure ─────────────────────────────────────────────────────
infra-up: ## Start local infrastructure (Kafka, Postgres, DynamoDB, observability)
	$(DC) up -d

infra-down: ## Stop and remove local infrastructure
	$(DC) down

infra-logs: ## Tail infrastructure logs
	$(DC) logs -f

kafka-topics: ## Create all required Kafka topics
	bash scripts/kafka-topics.sh

## ── Frontend ─────────────────────────────────────────────────────────────────
frontend-install: ## Install frontend dependencies
	cd frontend && $(NPM) install

frontend-dev: ## Start frontend dev server (http://localhost:5173)
	cd frontend && $(NPM) run dev

frontend-build: ## Build frontend for production
	cd frontend && $(NPM) run build

frontend-test: ## Run frontend tests
	cd frontend && $(NPM) run test

frontend-lint: ## Lint frontend code
	cd frontend && $(NPM) run lint

## ── Docker Images ────────────────────────────────────────────────────────────
docker-build: ## Build all service Docker images
	@for svc in gateway-service notification-service delivery-service template-service user-service audit-service; do \
	  echo "Building $$svc..."; \
	  docker build -t pulsenotify/$$svc:latest -f services/$$svc/Dockerfile . ; \
	done

docker-push: ## Push images to ECR (requires AWS_ACCOUNT_ID and AWS_REGION)
	bash scripts/docker-push.sh

## ── Setup ────────────────────────────────────────────────────────────────────
setup: ## Bootstrap local development environment
	bash scripts/setup-local.sh

## ── Deployment ───────────────────────────────────────────────────────────────
deploy-staging: ## Deploy to staging EKS cluster
	bash scripts/deploy.sh staging

deploy-prod: ## Deploy to production EKS cluster
	bash scripts/deploy.sh prod
