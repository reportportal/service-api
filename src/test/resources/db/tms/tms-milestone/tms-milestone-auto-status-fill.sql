-- Milestones for the automatic status rules (EPMRPP-109759)
INSERT INTO tms_milestone (id, "name", status, start_date, end_date, "type", project_id, display_id,
                           auto_start_on_start_date, auto_start_on_first_execution,
                           auto_complete_after_deadline)
VALUES (900, 'Start date reached', 'SCHEDULED', '2020-01-01 00:00:00', '2099-01-01 00:00:00', 'SPRINT', 1, 'M-900', true, false, false),
       (901, 'No options', 'SCHEDULED', '2020-01-01 00:00:00', '2020-01-31 00:00:00', 'SPRINT', 1, 'M-901', false, false, false),
       (902, 'Start date in the future', 'SCHEDULED', '2099-01-01 00:00:00', '2099-02-01 00:00:00', 'SPRINT', 1, 'M-902', true, false, false),
       (903, 'Deadline over', 'TESTING', '2020-01-01 00:00:00', '2020-01-31 00:00:00', 'RELEASE', 1, 'M-903', false, false, true),
       (904, 'Deadline in the future', 'TESTING', '2020-01-01 00:00:00', '2099-01-01 00:00:00', 'RELEASE', 1, 'M-904', false, false, true),
       (905, 'Executed test', 'SCHEDULED', '2099-01-01 00:00:00', '2099-02-01 00:00:00', 'SPRINT', 1, 'M-905', false, true, false),
       (906, 'Test to run only', 'SCHEDULED', '2099-01-01 00:00:00', '2099-02-01 00:00:00', 'SPRINT', 1, 'M-906', false, true, false),
       (907, 'Completed with all options', 'COMPLETED', '2020-01-01 00:00:00', '2099-01-01 00:00:00', 'RELEASE', 1, 'M-907', true, true, true),
       (908, 'Testing with start option', 'TESTING', '2020-01-01 00:00:00', '2099-01-01 00:00:00', 'SPRINT', 1, 'M-908', true, false, false);

INSERT INTO tms_test_plan (id, "name", description, project_id, display_id, milestone_id)
VALUES (905, 'Plan with executed test', 'Plan of milestone 905', 1, 'TP-905', 905),
       (906, 'Plan with test to run', 'Plan of milestone 906', 1, 'TP-906', 906);

INSERT INTO tms_test_folder (id, "name", description, project_id)
VALUES (905, 'Auto status folder', 'Folder for automatic status tests', 1);

INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (905, 'Auto status test case', 'Test case for automatic status', 905, 'HIGH', 1, 'TC-905');

INSERT INTO tms_test_case_version (id, test_case_id, name, is_default, is_draft)
VALUES (905, 905, 'Version 1', true, false);

INSERT INTO launch (id, uuid, project_id, user_id, name, description, start_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, launch_type, display_id, test_plan_id, retention_policy)
VALUES (905, '550e8400-e29b-41d4-a716-446655490905', 1, 1, 'Auto status launch 905', 'Launch of plan 905', '2026-01-01 10:00:00', 905, '2026-01-01 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-905', 905, 'REGULAR'),
       (906, '550e8400-e29b-41d4-a716-446655490906', 1, 1, 'Auto status launch 906', 'Launch of plan 906', '2026-01-01 10:00:00', 906, '2026-01-01 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0, 'MANUAL', 'L-906', 906, 'REGULAR');

INSERT INTO test_item (item_id, uuid, name, type, start_time, last_modified, path, unique_id, has_children, has_retries, has_stats, launch_id, test_case_hash)
VALUES (90905, '550e8400-e29b-41d4-a716-550000090905', 'Executed test', 'STEP', '2026-01-01 10:00:00', '2026-01-01 10:00:00', '90905', 'auto-status-905', false, false, true, 905, 9090905),
       (90906, '550e8400-e29b-41d4-a716-550000090906', 'Test to run', 'STEP', '2026-01-01 10:00:00', '2026-01-01 10:00:00', '90906', 'auto-status-906', false, false, true, 906, 9090906);

INSERT INTO test_item_results (result_id, status)
VALUES (90905, 'PASSED'),
       (90906, 'TO_RUN');

INSERT INTO tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
VALUES (90905, 905, 905, 90905, '{"id": 905, "name": "Auto status test case", "priority": "HIGH"}', 905),
       (90906, 905, 905, 90906, '{"id": 905, "name": "Auto status test case", "priority": "HIGH"}', 906);
