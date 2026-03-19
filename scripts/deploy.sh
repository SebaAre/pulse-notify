#!/usr/bin/env bash
set -euo pipefail

ENVIRONMENT=${1:?Usage: deploy.sh <staging|prod> [image-tag]}
IMAGE_TAG=${2:-latest}
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION:-us-east-1}.amazonaws.com"

echo "==> Deploying to $ENVIRONMENT (tag: $IMAGE_TAG)"

aws eks update-kubeconfig \
  --region "${AWS_REGION:-us-east-1}" \
  --name "pulsenotify-$ENVIRONMENT"

SERVICES=(gateway-service notification-service delivery-service template-service user-service audit-service)

for SVC in "${SERVICES[@]}"; do
  echo "  Updating $SVC..."
  kubectl set image deployment/"$SVC" \
    "$SVC=$ECR_REGISTRY/pulsenotify/$SVC:$IMAGE_TAG" \
    -n pulse-notify
done

echo "==> Waiting for rollouts..."
for SVC in "${SERVICES[@]}"; do
  kubectl rollout status deployment/"$SVC" -n pulse-notify --timeout=5m
done

echo "==> Deploy to $ENVIRONMENT complete."
