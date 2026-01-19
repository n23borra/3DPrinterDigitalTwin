/* =========================
   0. ENUMS & TYPES
   ========================= */


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

/* ========================================
   Printer inventory and telemetry history
   ======================================== */
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS printers
(
    id             UUID PRIMARY KEY,
    name           VARCHAR(120)            NOT NULL,
    type           VARCHAR(50)             NOT NULL,
    ip_address     VARCHAR(120)            NOT NULL,
    port           INTEGER,
    status         VARCHAR(30)             NOT NULL DEFAULT 'OFFLINE',
    last_heartbeat TIMESTAMPTZ,
    firmware       VARCHAR(120),
    metadata       JSONB
);

CREATE TABLE IF NOT EXISTS printer_snapshots
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    printer_id    UUID REFERENCES printers (id) ON DELETE CASCADE,
    ts            TIMESTAMPTZ NOT NULL DEFAULT now(),
    bed_temp      NUMERIC,
    nozzle_temp   NUMERIC,
    target_bed    NUMERIC,
    target_nozzle NUMERIC,
    progress      NUMERIC,
    z_height      NUMERIC,
    state         VARCHAR(50),
    raw_payload   JSONB
);

CREATE INDEX IF NOT EXISTS idx_printer_snapshots_printer_ts ON printer_snapshots (printer_id, ts DESC);

-- -- Minimal seed example to bootstrap UI when running locally
-- INSERT INTO printers (id, name, type, ip_address, port, status)
-- SELECT gen_random_uuid(), 'K2 Plus', 'MOONRAKER', '192.168.1.50', 7125, 'OFFLINE'
-- WHERE NOT EXISTS (SELECT 1 FROM printers);

-- Seed printer at initialization
INSERT INTO printers (id, name, type, ip_address, port, status)
SELECT gen_random_uuid(), 'K2 Plus', 'MOONRAKER', '10.29.232.179', 4408, 'OFFLINE'
WHERE NOT EXISTS (SELECT 1 FROM printers WHERE name = 'K2 Plus');

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
