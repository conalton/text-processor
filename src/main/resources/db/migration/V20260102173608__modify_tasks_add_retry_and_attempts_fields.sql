-- Migration: modify_tasks_add_retry_and_attempts_fields
-- Created at: 2026-01-02T17:36:08+01:00

ALTER TABLE tasks
    ADD COLUMN attempt_current INT NOT NULL DEFAULT 0 AFTER status,
    ADD COLUMN attempt_max     INT NOT NULL DEFAULT 3 AFTER attempt_current,
    ADD COLUMN next_attempt_at DATETIME(6) NULL AFTER attempt_max,
    ADD COLUMN status_prev     VARCHAR(32) NULL AFTER status,
    ADD COLUMN priority        INT NOT NULL DEFAULT 0 AFTER status_prev,
    ADD COLUMN locked_by       VARCHAR(64) NULL AFTER priority,
    ADD COLUMN locked_at       DATETIME(6) NULL AFTER locked_by,
    ADD COLUMN heartbeat_at    DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6) AFTER locked_at,
    ADD COLUMN cancel_at       DATETIME(6) NULL AFTER heartbeat_at;

ALTER TABLE tasks
    MODIFY COLUMN status VARCHAR(32) NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_status
    CHECK (status IN ('NEW','FILE_UPLOADED','FILE_IS_READY','PROCESSING','DONE','FAILED','CANCELLED')),
    ADD CONSTRAINT chk_tasks_status_prev
    CHECK (status_prev IS NULL OR status_prev IN ('NEW','FILE_UPLOADED','FILE_IS_READY','PROCESSING','DONE','FAILED','CANCELLED'));

CREATE INDEX idx_tasks_status_nextattempt_priority ON tasks (status, next_attempt_at, priority);
CREATE INDEX idx_tasks_locked_heartbeat ON tasks (locked_by, heartbeat_at);
CREATE INDEX idx_tasks_cancel_at ON tasks (cancel_at);
CREATE INDEX idx_tasks_pick
    ON tasks(status, locked_by, next_attempt_at, priority DESC);