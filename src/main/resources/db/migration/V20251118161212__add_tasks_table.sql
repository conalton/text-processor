-- Migration: add_tasks_table
-- Created at: 2025-11-18T16:12:12+01:00

CREATE TABLE `tasks` (
	`id` VARCHAR(36) NOT NULL COLLATE 'ascii_bin',
	`created_at` DATETIME(6) NOT NULL,
	`finished_at` DATETIME(6) NULL DEFAULT NULL,
	`meta` JSON NULL DEFAULT NULL,
	`result_path` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`source_path` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`status` ENUM('COMPLETED','FAILED','PENDING','PROCESSING') NOT NULL COLLATE 'ascii_bin',
     PRIMARY KEY (`id`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
;