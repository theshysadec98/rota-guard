CREATE TABLE staff (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    role            VARCHAR(100) NOT NULL,
    department      VARCHAR(100) NOT NULL,
    sensitivity_factor DOUBLE PRECISION NOT NULL DEFAULT 1.0
);

CREATE TABLE fatigue_policy (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    department              VARCHAR(100) NOT NULL,
    min_rest_hours          DOUBLE PRECISION NOT NULL DEFAULT 12,
    max_consecutive_nights  INT NOT NULL DEFAULT 2,
    max_weekly_hours        DOUBLE PRECISION NOT NULL DEFAULT 60,
    churn_threshold         INT NOT NULL DEFAULT 60
);

CREATE TABLE shift (
    id              BIGSERIAL PRIMARY KEY,
    staff_id        BIGINT NOT NULL REFERENCES staff (id),
    start_at        TIMESTAMPTZ NOT NULL,
    end_at          TIMESTAMPTZ NOT NULL,
    shift_type      VARCHAR(20) NOT NULL,
    revision_count  INT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shift_staff_start ON shift (staff_id, start_at);

CREATE TABLE analysis_run (
    id          BIGSERIAL PRIMARY KEY,
    week_start  DATE NOT NULL,
    policy_id   BIGINT NOT NULL REFERENCES fatigue_policy (id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    run_type    VARCHAR(20) NOT NULL DEFAULT 'FULL'
);

CREATE TABLE fatigue_score (
    id            BIGSERIAL PRIMARY KEY,
    run_id        BIGINT NOT NULL REFERENCES analysis_run (id),
    staff_id      BIGINT NOT NULL REFERENCES staff (id),
    total_points  INT NOT NULL,
    risk_level    VARCHAR(10) NOT NULL,
    churn_index   INT NOT NULL DEFAULT 0
);

CREATE TABLE violation (
    id             BIGSERIAL PRIMARY KEY,
    score_id       BIGINT NOT NULL REFERENCES fatigue_score (id),
    rule_code      VARCHAR(50) NOT NULL,
    severity       VARCHAR(20) NOT NULL,
    message        TEXT NOT NULL,
    evidence_json  TEXT NOT NULL
);

CREATE TABLE shift_revision (
    id           BIGSERIAL PRIMARY KEY,
    shift_id     BIGINT NOT NULL REFERENCES shift (id),
    changed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    change_type  VARCHAR(50) NOT NULL
);
