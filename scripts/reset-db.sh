#!/usr/bin/env bash
set -euo pipefail

echo "[reset-db] Stopping stack and removing volumes (DB will be wiped)..."
docker compose down -v

echo "[reset-db] Starting stack..."
docker compose up --build
