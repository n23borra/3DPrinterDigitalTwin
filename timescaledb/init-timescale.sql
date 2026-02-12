CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE printer_snapshots
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY,
    printer_id    UUID NOT NULL,
    ts            TIMESTAMPTZ NOT NULL,
    bed_temp      NUMERIC,
    nozzle_temp   NUMERIC,
    target_bed    NUMERIC,
    target_nozzle NUMERIC,
    progress      NUMERIC,
    z_height      NUMERIC,
    state         TEXT,
    raw_payload   JSONB,
    PRIMARY KEY (id, ts)
);

SELECT create_hypertable('printer_snapshots', 'ts');

CREATE INDEX ON printer_snapshots (printer_id, ts DESC);
