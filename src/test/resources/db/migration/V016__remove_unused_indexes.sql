DROP INDEX IF EXISTS log_uuid_idx;
ALTER TABLE log DROP CONSTRAINT IF EXISTS log_uuid_key;

DROP INDEX IF EXISTS launch_uuid_idx;
DROP INDEX IF EXISTS ti_uuid_idx;

DROP INDEX IF EXISTS test_case_hash_idx;
DROP INDEX IF EXISTS item_test_case_id_idx;
DROP INDEX IF EXISTS test_item_unique_id_idx;

DROP INDEX IF EXISTS pattern_item_pattern_id_idx;