# 3D-printer-digital-twin
Repository du projet de jumeau numérique pour les imprimantes 3D du FabLab (Creality K2 Plus en priorité, extensible à d'autres modèles).

## Contenu
Chaque répertoire important possède son propre `README.md` qui décrit les fichiers qu'il contient et leur rôle.

## Structure du dépôt
- `docs/` : documentation projet (voir `docs/README.md`).
    - `docs/architecture.md` : vue d'architecture détaillée (backend Java/Spring Boot, frontend React + Vite, PostgreSQL) et bonnes pratiques.
    - `docs/SETUP.md` : procédure d'installation détaillée.
- `backend/` : application Spring Boot (voir `backend/README.md`).
- `frontend/` : application React/Vite (voir `frontend/README.md`).
- `postgres/` : scripts d'initialisation PostgreSQL (voir `postgres/README.md`).
- `scripts/` : scripts utilitaires (voir `scripts/README.md`).

## Pistes d'amélioration
- **Gestion des logs** : journaliser les modifications fonctionnelles (qui a fait quoi, quand) plutôt que les connexions.
- **Export automatique des logs** : exporter de façon régulière (hebdomadaire et mensuelle) avec filtres par date, au format CSV.
- **Installation** : optimiser et simplifier l'installation pour les nouveaux utilisateurs (documentation et automatisation).

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
or 
```
docker exec -it 3dprinterdigitaltwin-postgres-1 psql -U twin -d twin
```

### Docker Compose (optionnel)
```
docker-compose up --build
```