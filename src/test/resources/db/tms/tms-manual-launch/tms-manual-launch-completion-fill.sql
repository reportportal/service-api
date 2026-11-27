-- =====================================================
-- Manual launches for the "Launch completion" filter (filter.eq.completion).
-- Loaded after tms-manual-launch-fill.sql. Project 1 (superadmin_personal).
--
-- Launches of the base fill: 100, 101, 102, 200 (Passed / Failed), 201 and 203 (with Skipped),
-- 202 (only In Progress).
--
-- 300: To Run + Passed
-- 301: To Run + In Progress
-- 302: only Skipped
-- 303: In Progress + Skipped
-- 304: To Run + Skipped + Failed
-- 305: no executions
-- =====================================================

INSERT INTO launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, launch_type, display_id, test_plan_id, retention_policy)
VALUES
    (300, '550e8400-e29b-41d4-a716-446655440300', 1, 1, 'Completion To Run and Passed',       NULL, '2024-02-01 10:00:00', NULL, 300, '2024-02-01 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-8',  NULL, 'REGULAR'),
    (301, '550e8400-e29b-41d4-a716-446655440301', 1, 1, 'Completion To Run and In Progress',  NULL, '2024-02-02 10:00:00', NULL, 301, '2024-02-02 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-9',  NULL, 'REGULAR'),
    (302, '550e8400-e29b-41d4-a716-446655440302', 1, 1, 'Completion Only Skipped',            NULL, '2024-02-03 10:00:00', NULL, 302, '2024-02-03 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-10', NULL, 'REGULAR'),
    (303, '550e8400-e29b-41d4-a716-446655440303', 1, 1, 'Completion In Progress and Skipped', NULL, '2024-02-04 10:00:00', NULL, 303, '2024-02-04 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-11', NULL, 'REGULAR'),
    (304, '550e8400-e29b-41d4-a716-446655440304', 1, 1, 'Completion To Run Skipped Failed',   NULL, '2024-02-05 10:00:00', NULL, 304, '2024-02-05 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-12', NULL, 'REGULAR'),
    (305, '550e8400-e29b-41d4-a716-446655440305', 1, 1, 'Completion No Executions',           NULL, '2024-02-06 10:00:00', NULL, 305, '2024-02-06 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-13', NULL, 'REGULAR');

INSERT INTO test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
VALUES
    (3000, '550e8400-e29b-41d4-a716-446655443000', 'Completion 300 To Run',      'manual.completion.3000', 'STEP', '2024-02-01 10:05:00', NULL, '2024-02-01 10:05:00', '3000', 'completion-3000', NULL, false, false, true, NULL, NULL, 300, 2839500),
    (3001, '550e8400-e29b-41d4-a716-446655443001', 'Completion 300 Passed',      'manual.completion.3001', 'STEP', '2024-02-01 10:05:00', NULL, '2024-02-01 10:05:00', '3001', 'completion-3001', NULL, false, false, true, NULL, NULL, 300, 2839501),
    (3010, '550e8400-e29b-41d4-a716-446655443010', 'Completion 301 To Run',      'manual.completion.3010', 'STEP', '2024-02-02 10:05:00', NULL, '2024-02-02 10:05:00', '3010', 'completion-3010', NULL, false, false, true, NULL, NULL, 301, 2839510),
    (3011, '550e8400-e29b-41d4-a716-446655443011', 'Completion 301 In Progress', 'manual.completion.3011', 'STEP', '2024-02-02 10:05:00', NULL, '2024-02-02 10:05:00', '3011', 'completion-3011', NULL, false, false, true, NULL, NULL, 301, 2839511),
    (3020, '550e8400-e29b-41d4-a716-446655443020', 'Completion 302 Skipped',     'manual.completion.3020', 'STEP', '2024-02-03 10:05:00', NULL, '2024-02-03 10:05:00', '3020', 'completion-3020', NULL, false, false, true, NULL, NULL, 302, 2839520),
    (3030, '550e8400-e29b-41d4-a716-446655443030', 'Completion 303 In Progress', 'manual.completion.3030', 'STEP', '2024-02-04 10:05:00', NULL, '2024-02-04 10:05:00', '3030', 'completion-3030', NULL, false, false, true, NULL, NULL, 303, 2839530),
    (3031, '550e8400-e29b-41d4-a716-446655443031', 'Completion 303 Skipped',     'manual.completion.3031', 'STEP', '2024-02-04 10:05:00', NULL, '2024-02-04 10:05:00', '3031', 'completion-3031', NULL, false, false, true, NULL, NULL, 303, 2839531),
    (3040, '550e8400-e29b-41d4-a716-446655443040', 'Completion 304 To Run',      'manual.completion.3040', 'STEP', '2024-02-05 10:05:00', NULL, '2024-02-05 10:05:00', '3040', 'completion-3040', NULL, false, false, true, NULL, NULL, 304, 2839540),
    (3041, '550e8400-e29b-41d4-a716-446655443041', 'Completion 304 Skipped',     'manual.completion.3041', 'STEP', '2024-02-05 10:05:00', NULL, '2024-02-05 10:05:00', '3041', 'completion-3041', NULL, false, false, true, NULL, NULL, 304, 2839541),
    (3042, '550e8400-e29b-41d4-a716-446655443042', 'Completion 304 Failed',      'manual.completion.3042', 'STEP', '2024-02-05 10:05:00', NULL, '2024-02-05 10:05:00', '3042', 'completion-3042', NULL, false, false, true, NULL, NULL, 304, 2839542);

INSERT INTO test_item_results (result_id, status, end_time, duration)
VALUES
    (3000, 'TO_RUN',      NULL,                  0),
    (3001, 'PASSED',      '2024-02-01 10:10:00', 300000),
    (3010, 'TO_RUN',      NULL,                  0),
    (3011, 'IN_PROGRESS', NULL,                  0),
    (3020, 'SKIPPED',     '2024-02-03 10:10:00', 0),
    (3030, 'IN_PROGRESS', NULL,                  0),
    (3031, 'SKIPPED',     '2024-02-04 10:10:00', 0),
    (3040, 'TO_RUN',      NULL,                  0),
    (3041, 'SKIPPED',     '2024-02-05 10:10:00', 0),
    (3042, 'FAILED',      '2024-02-05 10:10:00', 300000);

INSERT INTO tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
VALUES
    (300, 4, 1, 3000, '{"id": 4, "name": "User Login with Valid Credentials"}',   300),
    (301, 5, 2, 3001, '{"id": 5, "name": "User Login with Invalid Credentials"}', 300),
    (310, 4, 1, 3010, '{"id": 4, "name": "User Login with Valid Credentials"}',   301),
    (311, 5, 2, 3011, '{"id": 5, "name": "User Login with Invalid Credentials"}', 301),
    (320, 6, 3, 3020, '{"id": 6, "name": "Password Reset Flow"}',                 302),
    (330, 4, 1, 3030, '{"id": 4, "name": "User Login with Valid Credentials"}',   303),
    (331, 5, 2, 3031, '{"id": 5, "name": "User Login with Invalid Credentials"}', 303),
    (340, 4, 1, 3040, '{"id": 4, "name": "User Login with Valid Credentials"}',   304),
    (341, 5, 2, 3041, '{"id": 5, "name": "User Login with Invalid Credentials"}', 304),
    (342, 6, 3, 3042, '{"id": 6, "name": "Password Reset Flow"}',                 304);

SELECT setval('launch_id_seq',                  (SELECT COALESCE(MAX(id), 1) FROM launch));
SELECT setval('test_item_item_id_seq',          (SELECT COALESCE(MAX(item_id), 1) FROM test_item));
SELECT setval('tms_test_case_execution_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_execution));
