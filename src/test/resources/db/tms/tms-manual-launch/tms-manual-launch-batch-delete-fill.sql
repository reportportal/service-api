-- Committed (non-transactional) data for the batch delete test: batch delete runs each launch
-- in its own REQUIRES_NEW transaction, which cannot see data of an uncommitted test transaction.
INSERT INTO launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, launch_type, display_id, test_plan_id, retention_policy)
VALUES (9202, '550e8400-e29b-41d4-a716-446655449202', 1, 1, 'Manual Launch for Batch Deletion', 'Launch to be deleted by batch delete', '2024-01-17 10:00:00', NULL, 9202, '2024-01-17 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-9202', NULL, 'REGULAR');
