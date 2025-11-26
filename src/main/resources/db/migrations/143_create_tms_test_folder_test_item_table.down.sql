-- Rollback script for tms_test_folder_test_item table creation

DROP INDEX IF EXISTS idx_tms_test_folder_test_item_item;
DROP INDEX IF EXISTS idx_tms_test_folder_test_item_folder;
DROP TABLE IF EXISTS tms_test_folder_test_item;
