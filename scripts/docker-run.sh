#!/usr/bin/env bash
set -euo pipefail

: "${DOCKERHUB_USERNAME:?Set DOCKERHUB_USERNAME first}"
TAG="${IMAGE_TAG:-3.0}"

docker rm -f bankpro-banking-app 2>/dev/null || true
docker run -d \
  --name bankpro-banking-app \
  --restart unless-stopped \
  -p 8080:8080 \
  "${DOCKERHUB_USERNAME}/bankpro-banking-app:${TAG}"

echo "Application: http://localhost:8080/swagger-ui.html"
