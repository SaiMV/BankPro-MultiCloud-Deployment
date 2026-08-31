#!/usr/bin/env bash
set -euo pipefail

echo "=== BankPro build and test ==="
mvn -B clean test package

echo "=== Build complete ==="
ls -lh target/banking-app-1.0.0.jar
