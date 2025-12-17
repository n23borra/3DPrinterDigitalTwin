# 3D-printer-digital-twin

Jumeau numérique pour imprimantes 3D du FabLab (Creality K2 Plus en priorité, extensible à d'autres modèles).

## Stack
- Backend : Java 21 / Spring Boot (API REST) — port `8080`
- Frontend : React + Vite (build) servi par Nginx — port `80` (selon compose)
- Base : PostgreSQL

## Prérequis
- Docker + Docker Compose (Docker Desktop recommandé)

## Démarrage (recommandé) : Docker Compose

### 1) Configuration (obligatoire)
Créer un fichier `.env` à la racine (ne pas commiter) en partant de l’exemple :

```bash
cp .env.example .env
