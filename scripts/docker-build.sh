#!/usr/bin/env bash
set -euo pipefail

: "${DOCKERHUB_USERNAME:?Set DOCKERHUB_USERNAME first}"
TAG="${IMAGE_TAG:-3.0}"

mvn -B clean package
docker build -t "${DOCKERHUB_USERNAME}/bankpro-banking-app:${TAG}" .
