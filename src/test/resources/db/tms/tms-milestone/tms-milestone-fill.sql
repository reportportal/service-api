-- Milestones for CRUD operations
insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (100, 'Milestone 100', 'SCHEDULED',
        '2024-01-01 00:00:00.000', '2024-03-31 23:59:59.000', 'SPRINT', 1);

insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (101, 'Milestone 101 with Test Plans', 'TESTING',
        '2024-04-01 00:00:00.000', '2024-06-30 23:59:59.000', 'RELEASE', 1);

insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (102, 'Milestone 102 for Update', 'SCHEDULED',
        '2024-07-01 00:00:00.000', '2024-09-30 23:59:59.000', 'SPRINT', 1);

insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (103, 'Milestone 103 for Delete', 'TESTING',
        '2024-10-01 00:00:00.000', '2024-12-31 23:59:59.000', 'RELEASE', 1);

insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (104, 'Milestone 104 for Duplication', 'COMPLETED',
        '2025-01-01 00:00:00.000', '2025-03-31 23:59:59.000', 'SPRINT', 1);

insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (105, 'Milestone 105 for Patch', 'SCHEDULED',
        '2025-04-01 00:00:00.000', '2025-06-30 23:59:59.000', 'RELEASE', 1);

-- Additional milestone for test plan removal test
insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (106, 'Milestone 106 for Test Plan Removal', 'TESTING',
        '2025-07-01 00:00:00.000', '2025-09-30 23:59:59.000', 'SPRINT', 1);

-- Milestone with multiple test plans for comprehensive duplication test
insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (107, 'Milestone 107 with Multiple Test Plans', 'TESTING',
        '2025-10-01 00:00:00.000', '2025-12-31 23:59:59.000', 'RELEASE', 1);

-- Milestone for independence verification test
insert into tms_milestone (id, "name", status, start_date, end_date, "type", project_id)
values (108, 'Milestone 108 for Independence Test', 'SCHEDULED',
        '2026-01-01 00:00:00.000', '2026-03-31 23:59:59.000', 'SPRINT', 1);

-- Test plans for milestone 101
insert into tms_test_plan (id, "name", description, project_id, milestone_id)
values (400, 'Test Plan 400 in Milestone 101', 'First test plan in milestone', 1, 101),
       (401, 'Test Plan 401 in Milestone 101', 'Second test plan in milestone', 1, 101);

-- Test plan for milestone 106 (will be removed)
insert into tms_test_plan (id, "name", description, project_id, milestone_id)
values (405, 'Test Plan 405 in Milestone 106', 'Will be removed from milestone', 1, 106);

-- Test plans for milestone 107 (for comprehensive duplication)
insert into tms_test_plan (id, "name", description, project_id, milestone_id)
values (406, 'Test Plan 406 in Milestone 107', 'First plan for duplication', 1, 107),
       (407, 'Test Plan 407 in Milestone 107', 'Second plan for duplication', 1, 107),
       (408, 'Test Plan 408 in Milestone 107', 'Third plan for duplication', 1, 107);

-- Test plans for milestone 108 (for independence test)
insert into tms_test_plan (id, "name", description, project_id, milestone_id)
values (409, 'Test Plan 409 in Milestone 108', 'Plan in original milestone', 1, 108);

-- Test plan not in any milestone
insert into tms_test_plan (id, "name", description, project_id)
values (402, 'Test Plan 402 without Milestone', 'Test plan not in any milestone', 1);

-- Test plans for duplication test (in milestone 104)
insert into tms_test_plan (id, "name", description, project_id, milestone_id)
values (403, 'Test Plan 403 for Duplication', 'Will be duplicated with milestone', 1, 104),
       (404, 'Test Plan 404 for Duplication', 'Also will be duplicated', 1, 104);

-- Test folders for test plans
insert into tms_test_folder (id, "name", description, project_id)
values (400, 'Test Folder 400', 'Folder for milestone tests', 1),
       (401, 'Test Folder 401', 'Another folder for milestone tests', 1),
       (402, 'Test Folder 402', 'Third folder for milestone tests', 1);

-- Test cases for test plans in milestones
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (400, 'Test Case 400', 'Test case in plan 403', 400, 'HIGH', 'TC-400'),
       (401, 'Test Case 401', 'Test case in plan 403', 400, 'MEDIUM', 'TC-401'),
       (402, 'Test Case 402', 'Test case in plan 404', 401, 'LOW', 'TC-402'),
       (403, 'Test Case 403', 'Test case in plan 404', 401, 'HIGH', 'TC-403'),
       (404, 'Test Case 404', 'Test case in plan 406', 402, 'MEDIUM', 'TC-404'),
       (405, 'Test Case 405', 'Test case in plan 407', 402, 'LOW', 'TC-405'),
       (406, 'Test Case 406', 'Test case in plan 408', 402, 'HIGH', 'TC-406');

-- Add test cases to test plans
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (403, 400), (403, 401),  -- 2 test cases in plan 403
       (404, 402), (404, 403),  -- 2 test cases in plan 404
       (406, 404),  -- 1 test case in plan 406
       (407, 405),  -- 1 test case in plan 407
       (408, 406);  -- 1 test case in plan 408

-- Test case versions
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (400, 400, 'Default Version 400', true, false),
       (401, 401, 'Default Version 401', true, false),
       (402, 402, 'Default Version 402', true, false),
       (403, 403, 'Default Version 403', true, false),
       (404, 404, 'Default Version 404', true, false),
       (405, 405, 'Default Version 405', true, false),
       (406, 406, 'Default Version 406', true, false);

-- Manual scenarios
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (400, 400, 30, 'TEXT'),
       (401, 401, 25, 'TEXT'),
       (402, 402, 20, 'TEXT'),
       (403, 403, 15, 'TEXT'),
       (404, 404, 30, 'TEXT'),
       (405, 405, 25, 'TEXT'),
       (406, 406, 20, 'TEXT');

-- Manual scenario requirements
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number)
values ('REQ-400', 'Requirement for TC400', 400, 0),
       ('REQ-401', 'Requirement for TC401', 401, 0),
       ('REQ-402', 'Requirement for TC402', 402, 0),
       ('REQ-403', 'Requirement for TC403', 403, 0),
       ('REQ-404', 'Requirement for TC404', 404, 0),
       ('REQ-405', 'Requirement for TC405', 405, 0),
       ('REQ-406', 'Requirement for TC406', 406, 0);

-- Text manual scenarios
insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (400, 'Execute test case 400', 'Expected result for TC400'),
       (401, 'Execute test case 401', 'Expected result for TC401'),
       (402, 'Execute test case 402', 'Expected result for TC402'),
       (403, 'Execute test case 403', 'Expected result for TC403'),
       (404, 'Execute test case 404', 'Expected result for TC404'),
       (405, 'Execute test case 405', 'Expected result for TC405'),
       (406, 'Execute test case 406', 'Expected result for TC406');

-- Update sequences
SELECT setval('tms_milestone_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_milestone));
SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
