-- Migration: modify_task_statuses
-- Created at: 2025-11-23T19:51:00+01:00

ALTER TABLE `tasks`
  MODIFY COLUMN `status` ENUM('NEW',
                            'FILE_UPLOADED',
                            'PROCESSING',
                            'DONE',
                            'FAILED') NOT NULL;