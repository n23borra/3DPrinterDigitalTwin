/* =========================
   0. ENUMS & TYPES
   ========================= */
CREATE TYPE criticality_level AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE risk_status AS ENUM ('ACCEPT', 'MONITOR', 'TREAT');
CREATE TYPE action_status AS ENUM ('PLANNED', 'IN_PROGRESS', 'DONE');

/* =========================
   1. USERS & ROLES
   ========================= */
CREATE TABLE users
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50) UNIQUE  NOT NULL,
    email         VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    role          VARCHAR(30)         NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMP           NOT NULL DEFAULT now()
);

/* =========================
   2. ASSET PYRAMID
   ========================= */
CREATE TABLE asset_category
(
    id    SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    label VARCHAR(30) UNIQUE NOT NULL
);

INSERT INTO asset_category (label)
VALUES ('Physical'),
       ('Telecom'),
       ('ICT/AI'),
       ('Services'),
       ('Organization/Process'),
       ('Users');

CREATE TABLE category_admin
(
    category_id SMALLINT REFERENCES asset_category (id) ON DELETE CASCADE,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (category_id, user_id)
);

/* =========================
   3. ANALYSIS
   ========================= */
CREATE TABLE analysis
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(120)      NOT NULL,
    description TEXT,
    language    VARCHAR(5)                 DEFAULT 'fr',
    scope       TEXT,
    criticality criticality_level NOT NULL,
    s1          INTEGER           NOT NULL,
    s2          INTEGER           NOT NULL,
    dm          SMALLINT CHECK (dm BETWEEN 1 AND 5),
    ta          SMALLINT CHECK (ta BETWEEN 1 AND 5),
    owner_id    BIGINT REFERENCES users (id),
    created_at  TIMESTAMP         NOT NULL DEFAULT now()
);

/* =========================
   4. ASSETS & IMPACTS
   ========================= */
CREATE TABLE asset
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    analysis_id BIGINT NOT NULL REFERENCES analysis (id) ON DELETE CASCADE,
    category_id SMALLINT NOT NULL REFERENCES asset_category (id),
    name        VARCHAR(120) NOT NULL,
    description TEXT,
    impact_c    SMALLINT CHECK (impact_c BETWEEN 0 AND 4), -- Confidentiality
    impact_i    SMALLINT CHECK (impact_i BETWEEN 0 AND 4), -- Integrity
    impact_a    SMALLINT CHECK (impact_a BETWEEN 0 AND 4), -- Availability
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE asset_dependency
(
    parent_asset BIGINT REFERENCES asset (id) ON DELETE CASCADE,
    child_asset  BIGINT REFERENCES asset (id) ON DELETE CASCADE,
    PRIMARY KEY (parent_asset, child_asset)
);

CREATE TABLE asset_reference
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID UNIQUE,
    code        VARCHAR(50),
    label       TEXT          NOT NULL,
    description TEXT,
    type        VARCHAR(30),
    language    VARCHAR(5),
    version     SMALLINT
);

/* =========================
   5. THREAT REFERENCE & RELATED
   ========================= */
DROP TABLE IF EXISTS threat CASCADE;
CREATE TABLE threat
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID UNIQUE,
    code        VARCHAR(20),
    label       TEXT          NOT NULL,
    description TEXT,
    theme       TEXT,
    language    VARCHAR(5),
    a           BOOLEAN,
    c           BOOLEAN,
    i           BOOLEAN,
    probability NUMERIC(5, 2) CHECK (probability BETWEEN 0 AND 1)
);

DROP TABLE IF EXISTS vulnerability CASCADE;
CREATE TABLE vulnerability
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID UNIQUE,
    code        VARCHAR(20),
    label       TEXT NOT NULL,
    description TEXT,
    mode        SMALLINT,
    status      SMALLINT,
    gravity     NUMERIC(5, 2) CHECK (gravity BETWEEN 0 AND 1)
);

DROP TABLE IF EXISTS control CASCADE;

CREATE TABLE control
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid              UUID UNIQUE,
    code              VARCHAR(100),
    label             TEXT          NOT NULL,
    description       TEXT,
    meta              JSONB,
    category          TEXT,
    referential       UUID,
    referential_label VARCHAR(60),
    efficiency        NUMERIC(5, 2) NOT NULL DEFAULT 1.0 CHECK (efficiency BETWEEN 0 AND 1)
);

CREATE INDEX idx_control_label ON control (label);


/* =========================
   6. INHERENT RISK (ASSET+THREAT+VULN)
   ========================= */
CREATE TABLE risk_base
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id         BIGINT REFERENCES asset (id) ON DELETE CASCADE,
    threat_id        BIGINT REFERENCES threat (id),
    vulnerability_id BIGINT REFERENCES vulnerability (id),
    CONSTRAINT uq_risk_combi UNIQUE (asset_id, threat_id, vulnerability_id)
);

/* =========================
   7. APPLIED CONTROLS
   ========================= */
CREATE TABLE control_implementation
(
    risk_id    BIGINT REFERENCES risk_base (id) ON DELETE CASCADE,
    control_id BIGINT REFERENCES control (id),
    level      NUMERIC(2, 1) NOT NULL CHECK (level IN (0, 0.5, 1)),
    PRIMARY KEY (risk_id, control_id)
);

/* =========================
   8. CALCULATION RESULT
   ========================= */
CREATE TABLE risk_result
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    risk_id   BIGINT REFERENCES risk_base (id) ON DELETE CASCADE,
    r0        NUMERIC,
    r1        NUMERIC,
    r2        NUMERIC,
    r3        NUMERIC,
    fr        NUMERIC,
    status    risk_status,
    last_calc TIMESTAMP NOT NULL DEFAULT now()
);

/* =========================
   9. ACTION PLAN
   ========================= */
CREATE TABLE treatment_plan
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    risk_id        BIGINT REFERENCES risk_result (id) ON DELETE CASCADE,
    strategy       VARCHAR(15) CHECK (strategy IN ('MITIGATE', 'TRANSFER', 'ELIMINATE', 'ACCEPT')),
    description    TEXT,
    responsible_id BIGINT REFERENCES users (id),
    due_date       DATE,
    status         action_status NOT NULL DEFAULT 'PLANNED',
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    closed_at      TIMESTAMP
);

/* =========================
   10. AUDIT LOG
   ========================= */
CREATE TABLE audit_log
(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id  BIGINT REFERENCES users (id),
    action   TEXT,
    details  TEXT,
    log_time TIMESTAMP NOT NULL DEFAULT now()
);

/* =========================
   11. USEFUL INDEXES
   ========================= */
CREATE INDEX idx_riskresult_status ON risk_result (status);
CREATE INDEX idx_riskresult_fr ON risk_result (fr);
CREATE INDEX idx_asset_category ON asset (category_id);
CREATE INDEX idx_control_level ON control_implementation (level);

/* =========================
   12. SEED ADMIN CATEGORIES
   (Run after creating
    the admin accounts)
   ========================= */
/*
-- Example :
INSERT INTO users (username, email, password_hash, role)
VALUES ('admin_Physical', 'phy@org.local', 'hash', 'CAT_ADMIN');

INSERT INTO category_admin (category_id, user_id)
SELECT id, u.id
FROM asset_category ac
JOIN users u ON u.username = 'admin_' || ac.label;
*/
