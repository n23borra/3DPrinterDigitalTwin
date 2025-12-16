# Setup (Docker)

## Prérequis
- Docker Desktop (ou Docker Engine + Compose)

## Démarrage (1ère fois)
1) Créer le fichier `.env` :
    - Copier `.env.example` → `.env`
    - Renseigner `JWT_SECRET` (obligatoire)

2) Lancer :
   docker compose up --build

## En cas d'erreurs DB (migrations/ddl-auto)
Si PostgreSQL contient déjà un schéma incompatible (volume persistant), reset :
- Linux/macOS/WSL : ./scripts/reset-db.sh
- Windows : .\scripts\reset-db.ps1

Attention : cette opération supprime les données de la base (volume).
