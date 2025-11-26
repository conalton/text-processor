-- Migration: add_task_version
-- Created at: 2025-11-23T20:17:04+01:00

ALTER TABLE `tasks`
    ADD COLUMN `version` BIGINT NOT NULL DEFAULT 0;