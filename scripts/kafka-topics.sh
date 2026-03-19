#!/usr/bin/env bash
set -euo pipefail

BOOTSTRAP="${KAFKA_BROKERS:-localhost:9092}"
REPLICATION="${REPLICATION_FACTOR:-1}"  # 1 for local, 3 for MSK

echo "==> Creating Kafka topics on $BOOTSTRAP"

create_topic() {
  local name=$1
  local partitions=${2:-12}
  local retention_ms=${3:-604800000}  # 7 days default

  docker compose exec kafka kafka-topics \
    --bootstrap-server "$BOOTSTRAP" \
    --create \
    --if-not-exists \
    --topic "$name" \
    --partitions "$partitions" \
    --replication-factor "$REPLICATION" \
    --config "retention.ms=$retention_ms"

  echo "  Created: $name"
}

create_topic "notification.requested"   12
create_topic "notification.processed"   12
create_topic "delivery.attempted"       12
create_topic "delivery.completed"       12
create_topic "delivery.failed"          12 2592000000   # 30 days
create_topic "delivery.failed.dlq"       3 2592000000

echo "==> Done. Topics:"
docker compose exec kafka kafka-topics --bootstrap-server "$BOOTSTRAP" --list
