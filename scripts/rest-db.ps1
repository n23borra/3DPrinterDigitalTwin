Write-Host "[reset-db] Stopping stack and removing volumes (DB will be wiped)..."
docker compose down -v

Write-Host "[reset-db] Starting stack..."
docker compose up --build
