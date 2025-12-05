# Architecture du jumeau numérique des imprimantes 3D

## 1. Vue d'ensemble
### Objectifs clés
- Collecter et historiser des données des imprimantes sans perturber leur fonctionnement (lecture non intrusive, commandes limitées).
- Normaliser les données issues de connecteurs hétérogènes (Moonraker/Klipper, OctoPrint, etc.) vers un modèle commun stocké dans PostgreSQL.
- Exposer des API REST claires pour le frontend React (Vite) et préparer l'arrivée du temps réel (WebSocket ou SSE si disponible).
- Faciliter l'extension à de nouveaux modèles d'imprimantes et à de nouveaux modules d'analyse/maintenance.

### Découpage haut niveau (backend Java Spring Boot proposé)
- **Connector Layer** : `PrinterConnector` (interface) + implémentations (ex. `MoonrakerConnector`, `OctoPrintConnector`). Gestion du polling et/ou WebSocket. Responsable d'adapter l'API externe vers un DTO brut interne.
- **Normalization Layer** : `DataNormalizationService` convertit les données brutes de chaque connecteur vers un modèle standard (domain events, mesures normalisées) et gère l'enrichissement (ex. horodatage, mapping états).
- **Domain Services** :
    - `PrinterPollingService` (orchestration du polling, scheduling).
    - `MetricsService` / `HistoryService` (agrégation, requêtes d'historique, statistiques de base).
    - `AlertService` (détection d'anomalies, règles d'alerte, messages pédagogiques).
    - `MaintenanceService` (règles/échéances, suggestions prédictives simples à partir des historiques).
    - `AuthService` / `UserService` (authentification, rôles).
- **Persistence Layer** : Repositories JPA (PostgreSQL) pour les entités du domaine.
- **API Layer** : Contrôleurs REST exposant les endpoints pour le frontend. Adaptateurs vers WebSocket/SSE possibles pour le temps réel.

### Découpage haut niveau (frontend React + Vite, JSX)
- **Pages** : Dashboard, Historique, Maintenance, Administration, Login.
- **Composants clés** : cartes de température, barre de progression, timeline/log d'alertes, graphiques, tableaux d'impressions, formulaires (login, ajout imprimante, création utilisateur), toasts/notifications.
- **Services API** : `apiClient` générique (fetch/axios), services spécifiques (`printerApi`, `metricsApi`, `maintenanceApi`, `authApi`).
- **Gestion d'état** : hooks personnalisés (`usePrinterFeed`, `useAuth`) et context global léger (auth + préférences UI). Prévoir WebSocket/SSE plus tard.

### Multi-imprimantes et extensibilité des connecteurs
- `Printer` possède un type (`CREALITY_K2`, `ULTIMAKER`, `GENERIC_OCTOPRINT`...), qui sélectionne dynamiquement l'implémentation `PrinterConnector` via une factory/registry.
- L'ajout d'un nouveau connecteur se fait en implémentant l'interface `PrinterConnector` + un mapper de normalisation, sans toucher au reste du domaine ni au frontend (qui consomme le modèle normalisé).

---

## 2. Modélisation backend (Java)
### Découpage des packages (exemple)
- `config` : configuration Spring, sécurité, scheduling, datasources.
- `controller` : REST controllers.
- `service` : services métier (polling, metrics, alerts, maintenance, auth).
- `domain/model` : entités JPA (Printer, PrinterSnapshot, PrintJob, ErrorEvent, MaintenanceTask, User, Role...).
- `domain/dto` : DTO exposés par l'API, payloads d'entrée/sortie.
- `repository` : interfaces JPA.
- `printer/connector` : interface `PrinterConnector`, implémentations (Moonraker, OctoPrint...), clients HTTP/WebSocket.
- `security` : configuration JWT/session, filtres, providers.
- `util` : helpers (mapping, date/time, pagination).

### Entités principales (extraits)
- **Printer** : `id (UUID)`, `name`, `type`, `ip`, `port`, `status` (IDLE/PRINTING/PAUSED/OFFLINE), `lastHeartbeat`, `firmware`, `metadata JSONB`.
- **PrinterSnapshot** : `id`, `printer_id (FK)`, `timestamp`, `bedTemp`, `nozzleTemp`, `targetBed`, `targetNozzle`, `progress`, `layer`, `zHeight`, `state`, `rawPayload JSONB`.
- **PrintJob** : `id`, `printer_id`, `fileName`, `material`, `startedAt`, `endedAt`, `durationSec`, `status`, `notes`.
- **ErrorEvent / Alert** : `id`, `printer_id`, `type` (TEMP_DRIFT, COMM_ERROR, USER_INPUT), `severity` (INFO/WARN/CRITICAL), `message`, `details JSONB`, `createdAt`, `acknowledged`.
- **MaintenanceRule** : `id`, `printer_type`, `trigger` (heures/impressions/condition capteur), `threshold`, `description`, `actionHint`.
- **MaintenanceTask** : `id`, `printer_id`, `rule_id`, `dueAt`, `status` (PENDING/DONE/WAIVED), `note`.
- **User** : `id`, `email`, `passwordHash`, `fullName`, `active`.
- **Role** : `id`, `name` (ADMIN/TECH/USER) ; **UserRole** : `user_id`, `role_id`.

### Schéma PostgreSQL simplifié
- `printers(id PK, name, type, ip, port, status, last_heartbeat, firmware, metadata JSONB)`
- `printer_snapshots(id PK, printer_id FK->printers, ts timestamptz, bed_temp, nozzle_temp, target_bed, target_nozzle, progress, layer, z_height, state, raw_payload JSONB)`
- `print_jobs(id PK, printer_id FK->printers, file_name, material, started_at, ended_at, duration_sec, status, notes)`
- `error_events(id PK, printer_id FK->printers, type, severity, message, details JSONB, created_at, acknowledged)`
- `maintenance_rules(id PK, printer_type, trigger, threshold, description, action_hint)`
- `maintenance_tasks(id PK, printer_id FK->printers, rule_id FK->maintenance_rules, due_at, status, note)`
- `users(id PK, email UNIQUE, password_hash, full_name, active)`
- `roles(id PK, name UNIQUE)` ; `user_roles(user_id FK->users, role_id FK->roles, PK(user_id, role_id))`

### Services clés (interfaces/pseudo-code)
- **PrinterConnector**
  ```java
  interface PrinterConnector {
      PrinterType getType();
      ConnectorCapabilities getCapabilities();
      RawPrinterState fetchState(Printer printer); // polling HTTP
      RawPrinterState subscribeState(Printer printer, Consumer<RawPrinterState> cb); // optionnel WebSocket
      void sendCommand(Printer printer, String gcodeOrAction);
  }
  ```
- **PrinterPollingService** : scheduler qui récupère la liste des imprimantes actives, interroge le connecteur adéquat (polling interval configurable, backoff si échec), publie des `RawPrinterState`.
- **DataNormalizationService** : transforme `RawPrinterState` en `PrinterSnapshot` + événements (erreurs, alertes), applique mapping d'états, arrondi des valeurs, enrichissement (timestamps). Peut déclencher des règles de maintenance/alerte.
- **MetricsService / HistoryService** : lecture historisée, agrégations (moyennes, min/max, nombre d'erreurs par période, temps de chauffe moyen, MTBF approximatif).
- **AlertService** : règles (ex. sur-température, absence de heartbeat, progression bloquée), génération d'`ErrorEvent` + message pédagogique.
- **MaintenanceService** : évaluation des `MaintenanceRule` sur l'historique (heures de chauffe cumulées, nombre d'impressions, variation de température), création/MAJ des `MaintenanceTask`.
- **AuthService / UserService** : login (JWT ou session), gestion des rôles, vérification des permissions sur endpoints sensibles (envoi de G-code, création d'utilisateur, gestion règles).

### Endpoints REST (échantillon)
- `GET /api/printers` → liste des imprimantes (id, nom, type, statut, dernier heartbeat).
- `POST /api/printers` (ADMIN) → ajouter une imprimante (ip, port, type, nom, metadata).
- `GET /api/printers/{id}/state` → dernier snapshot normalisé.
- `GET /api/printers/{id}/history?from=&to=&granularity=` → séries historiques.
- `POST /api/printers/{id}/command` (TECH/ADMIN) → envoi G-code ou commande sécurisée.
- `GET /api/stats/errors?range=7d` → statistiques d'erreurs/alertes.
- `GET /api/maintenance/tasks` → tâches ouvertes/à venir.
- `POST /api/maintenance/tasks/{id}/complete` → clôturer une tâche.
- `POST /api/auth/login` → retour token + rôles ; `GET /api/users/me` → profil courant.

---

## 3. Modélisation frontend (React + Vite)
### Structure de projet (proposition)
```
src/
  main.jsx
  App.jsx
  routes.jsx
  components/
    metrics/TemperatureCard.jsx
    metrics/ProgressBar.jsx
    alerts/AlertFeed.jsx
    charts/HistoryChart.jsx
    printers/PrinterTable.jsx
    forms/LoginForm.jsx
    forms/PrinterForm.jsx
  pages/
    Dashboard.jsx
    History.jsx
    Maintenance.jsx
    Admin.jsx
    Login.jsx
  services/
    apiClient.js
    printerApi.js
    metricsApi.js
    maintenanceApi.js
    authApi.js
  hooks/
    useAuth.js
    useLivePrinter.js (polling ou WebSocket)
  context/
    AuthContext.jsx
  styles/
    (CSS modules ou Tailwind selon choix)
```

### Pages et responsabilités
- **Dashboard** : état temps réel multi-imprimantes (températures, progression, statut, alertes en cours). Polling léger (ex. 5-10s) ou WebSocket quand dispo.
- **Historique** : graphiques (températures, progression), tableau des impressions passées, filtres par période/imprimante, export CSV.
- **Maintenance** : liste des règles, tâches planifiées, accusé de réalisation, vue pédagogique (pourquoi cette tâche est recommandée).
- **Administration** : gestion utilisateurs/roles, gestion des imprimantes (ajout, modification IP/port/type), configuration des règles d'alerte.
- **Login** : authentification + stockage du token (localStorage) via `useAuth`.

### Composants clés
- **TemperatureCard** (buse/bed, valeur actuelle + cible, couleur par seuil, infobulle pédagogique si hors plage pour le matériau choisi).
- **ProgressBar** (progression impression + temps estimé restant).
- **AlertFeed** (liste temps réel des alertes, boutons ack, filtre par sévérité).
- **HistoryChart** (wrapper Recharts ou Chart.js pour séries température/progression).
- **PrinterTable** (états multi-imprimantes, actions limitées : pause/reprendre si autorisé).
- **Formulaires** : `LoginForm`, `PrinterForm`, `UserForm`, `RuleForm`.

### Services API
- `apiClient` : wrapper fetch/axios avec baseURL, gestion du token, interceptors d'erreur 401.
- `printerApi` : `list()`, `getState(id)`, `getHistory(id, params)`, `sendCommand(id, payload)`.
- `metricsApi` : stats globales, erreurs.
- `maintenanceApi` : `listRules()`, `listTasks()`, `completeTask(id)`.
- `authApi` : `login(credentials)`, `me()`.

---

## 4. Modularité & extensibilité
### Nouveaux types d'imprimantes
- **Pattern** : interface `PrinterConnector` + implémentations spécifiques. Enregistrement dans une factory (`ConnectorRegistry`) mappée par `PrinterType`.
- **Moonraker/Klipper** : impl `MoonrakerConnector` (HTTP GET, WebSocket event stream si dispo) + mapper Moonraker → modèle standard.
- **OctoPrint/Ultimaker** : impl `OctoPrintConnector` (API REST + WebSocket), conversions propres. Possibilité de réutiliser une partie commune (auth tokens, pagination, parsing des events).
- **Frontière** :
    - Spécifique imprimante : clients HTTP/WebSocket, mapping des champs bruts (`temperature.actual`, `temperature.target`, `job.progress`...).
    - Générique : entités, règles de maintenance/alertes, API REST, base de données, UI (consomme uniquement les champs normalisés).

### Nouveaux modules d'analyse/maintenance
- Ajouter un service dédié (ex. `PredictiveMaintenanceService`) branché sur l'historique via `MetricsService`. Les nouvelles règles écrivent dans `maintenance_tasks` et `error_events` sans changer les connecteurs.
- Event bus interne possible (Spring Application Events) pour consommer les `PrinterSnapshot` et appliquer des analytiques sans modifier le polling.

---

## 5. Aspects non fonctionnels & bonnes pratiques
### Ne pas perturber les imprimantes
- Polling raisonnable (ex. 5-10s), backoff exponentiel en cas d'erreur réseau.
- Timeouts HTTP/WebSocket configurés, limitation du débit de commandes (rate limiting côté API backend).
- Journalisation (niveau DEBUG pour payloads, INFO pour opérations, WARN/ERROR pour anomalies) avec masquage des secrets.
- Pas d'envoi automatique de commandes sauf si explicitement déclenché (et sous permissions TECH/ADMIN).

### Sécurité
- Authentification JWT ou session serveur ; stockage token côté frontend (localStorage) + refresh périodique.
- Rôles/permissions par endpoint (Spring Security) ; audit des actions sensibles (envoi G-code, création utilisateur).
- Communication HTTPS en production ; CORS configuré pour le domaine du frontend.

### Maintenabilité & documentation
- Monorepo léger : `backend/`, `frontend/`, `docs/`. CI basique (lint + tests unitaires).
- Conventions : Java package par feature, noms explicites, DTO séparés des entités, mapper (MapStruct possible).
- Documentation : `README` racine (démarrage rapide), `docs/architecture.md` (ce document), `docs/api.md` (endpoints détaillés), `docs/connectors/` (guides d'implémentation par modèle).

### Squelette README (proposition)
```
# 3D Printer Digital Twin

## Architecture
- Backend : Java Spring Boot (REST, PostgreSQL), connecteurs imprimante via interface `PrinterConnector`.
- Frontend : React + Vite (JSX), appels REST + websocket futur.
- BDD : PostgreSQL.

## Démarrer en local
1. `docker compose up -d db` (PostgreSQL) ou installer Postgres local.
2. Backend : `cd backend && ./mvnw spring-boot:run` (configuration `application.yml` avec IP/port des imprimantes).
3. Frontend : `cd frontend && npm install && npm run dev`.

## Ajouter une nouvelle imprimante
- Créer une entrée `Printer` en DB ou via `POST /api/printers` (type, IP, port).
- Implémenter si besoin un nouveau `PrinterConnector` dans `printer/connector` et l'enregistrer dans la factory.

## Documentation
- Voir `docs/architecture.md` pour la vue globale.
- (Futur) `docs/api.md` pour les endpoints détaillés.
```