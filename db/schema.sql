CREATE TABLE IF NOT EXISTS roles (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     email VARCHAR(255) UNIQUE NOT NULL,
                                     password_hash TEXT NOT NULL,
                                     full_name TEXT,
                                     active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                                          role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
                                          PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS printers (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        name TEXT NOT NULL,
                                        type VARCHAR(64) NOT NULL,
                                        ip TEXT,
                                        port INTEGER,
                                        status VARCHAR(32) NOT NULL,
                                        last_heartbeat TIMESTAMPTZ,
                                        firmware TEXT,
                                        metadata JSONB
);
CREATE INDEX IF NOT EXISTS idx_printers_status ON printers(status);

CREATE TABLE IF NOT EXISTS printer_snapshots (
                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 printer_id UUID NOT NULL REFERENCES printers(id) ON DELETE CASCADE,
                                                 timestamp TIMESTAMPTZ NOT NULL,
                                                 bed_temp DOUBLE PRECISION,
                                                 nozzle_temp DOUBLE PRECISION,
                                                 target_bed DOUBLE PRECISION,
                                                 target_nozzle DOUBLE PRECISION,
                                                 progress DOUBLE PRECISION,
                                                 layer INTEGER,
                                                 z_height DOUBLE PRECISION,
                                                 state VARCHAR(32),
                                                 raw_payload JSONB
);
CREATE INDEX IF NOT EXISTS idx_snapshots_printer_ts ON printer_snapshots(printer_id, timestamp DESC);

CREATE TABLE IF NOT EXISTS print_jobs (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          printer_id UUID NOT NULL REFERENCES printers(id) ON DELETE CASCADE,
                                          file_name TEXT,
                                          material TEXT,
                                          started_at TIMESTAMPTZ,
                                          ended_at TIMESTAMPTZ,
                                          duration_sec BIGINT,
                                          status VARCHAR(32),
                                          notes TEXT
);
CREATE INDEX IF NOT EXISTS idx_print_jobs_printer ON print_jobs(printer_id);

CREATE TABLE IF NOT EXISTS error_events (
                                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                            printer_id UUID NOT NULL REFERENCES printers(id) ON DELETE CASCADE,
                                            type VARCHAR(64),
                                            severity VARCHAR(32),
                                            message TEXT NOT NULL,
                                            details JSONB,
                                            created_at TIMESTAMPTZ NOT NULL,
                                            acknowledged BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_error_events_printer ON error_events(printer_id);
CREATE INDEX IF NOT EXISTS idx_error_events_created_at ON error_events(created_at);

CREATE TABLE IF NOT EXISTS maintenance_rules (
                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 printer_type VARCHAR(64) NOT NULL,
                                                 trigger TEXT NOT NULL,
                                                 threshold DOUBLE PRECISION NOT NULL,
                                                 description TEXT,
                                                 action_hint TEXT
);

CREATE TABLE IF NOT EXISTS maintenance_tasks (
                                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 printer_id UUID NOT NULL REFERENCES printers(id) ON DELETE CASCADE,
                                                 rule_id UUID NOT NULL REFERENCES maintenance_rules(id) ON DELETE CASCADE,
                                                 due_at TIMESTAMPTZ NOT NULL,
                                                 status VARCHAR(32),
                                                 note TEXT
);
CREATE INDEX IF NOT EXISTS idx_tasks_due_at ON maintenance_tasks(due_at);