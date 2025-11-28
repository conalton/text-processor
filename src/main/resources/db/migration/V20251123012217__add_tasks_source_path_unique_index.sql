-- Migration: add_tasks_source_path_unique_index
-- Created at: 2025-11-23T01:22:17+01:00

ALTER TABLE `tasks` ADD UNIQUE INDEX `source_path` (`source_path`);