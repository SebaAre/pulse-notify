#!/usr/bin/env bash
set -euo pipefail

echo "==> PulseNotify Local Setup"

# Check prerequisites
command -v java   >/dev/null 2>&1 || { echo "Java 21 required"; exit 1; }
command -v mvn    >/dev/null 2>&1 || { echo "Maven required"; exit 1; }
command -v node   >/dev/null 2>&1 || { echo "Node.js 20 required"; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Docker required"; exit 1; }

echo "==> Starting infrastructure..."
docker compose up -d

echo "==> Waiting for Kafka to be ready..."
until docker compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; do
  sleep 3
done

echo "==> Creating Kafka topics..."
bash scripts/kafka-topics.sh

echo "==> Installing frontend dependencies..."
(cd frontend && npm install)

echo ""
echo "==> Setup complete!"
echo "    Run services:  cd services/<name> && ../mvnw spring-boot:run"
echo "    Frontend dev:  make frontend-dev"
echo "    Grafana:       http://localhost:3000  (admin/admin)"
echo "    Kafka UI:      http://localhost:8090"
echo "    DynamoDB UI:   http://localhost:8001"
