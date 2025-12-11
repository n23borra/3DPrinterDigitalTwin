# 3D-printer-digital-twin
Repository du projet de jumeau numérique pour les imprimantes 3D du FabLab (Creality K2 Plus en priorité, extensible à d'autres modèles).

## Contenu
- `docs/architecture.md` : vue d'architecture détaillée (backend Java/Spring Boot, frontend React + Vite, PostgreSQL) et bonnes pratiques.
- (Futur) `backend/` : application Spring Boot (connecteurs imprimantes, API REST, stockage PostgreSQL).
- (Futur) `frontend/` : application React/Vite (dashboard, historique, maintenance, administration).

## Démarrage rapide (esquisse)
1. Installer PostgreSQL ou utiliser un conteneur Docker (`docker compose up -d db` si un fichier compose est fourni ultérieurement).
2. Backend : `cd backend && ./mvnw spring-boot:run` (une fois le module créé et configuré).
3. Frontend : `cd frontend && npm install && npm run dev`.

Pour plus de détails et la roadmap d'implémentation, consulter `docs/architecture.md`.


### Backend
```
cd backend
mvn spring-boot:run
```

### Frontend
```
cd frontend
npm install
npm run dev
```

### Base de données

```
psql -h localhost -U twin -d twin -f db/schema.sql
```

### Docker Compose (optionnel)
```
docker-compose up --build
```