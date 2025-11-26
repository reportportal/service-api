-- Rollback script for tms_step_execution table creation

DROP INDEX IF EXISTS idx_tms_step_execution_tms_step;
DROP INDEX IF EXISTS idx_tms_step_execution_launch;
DROP INDEX IF EXISTS idx_tms_step_execution_test_item;
DROP INDEX IF EXISTS idx_tms_step_execution_test_case;
DROP TABLE IF EXISTS tms_step_execution;
