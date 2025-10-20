insert into tms_product_version (id, documentation, "version", project_id)
values (3, 'documentation3', 'version3', 1);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (3, 'milestone3', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type3', 3);

insert into tms_environment (id, "name", project_id)
values (3, 'name3', 1);

insert into tms_attribute (id, "key")
values (3, 'value3');

insert into tms_product_version (id, documentation, "version", project_id)
values (4, 'documentation4', 'version4', 1);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (4, 'milestone4', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type4', 4);

insert into tms_environment (id, "name", project_id)
values (4, 'name4', 1);

insert into tms_attribute (id, "key")
values (4, 'value4');

insert into tms_test_plan (id, "name", description, project_id, environment_id, product_version_id)
values (4, 'name4', 'description4', 1, 4, 4);

insert into tms_product_version (id, documentation, "version", project_id)
values (5, 'documentation5', 'version5', 1);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (5, 'milestone5', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type4', 5);

insert into tms_environment (id, "name", project_id)
values (5, 'name5', 1);

insert into tms_attribute (id, "key")
values (5, 'value5');

insert into tms_test_plan (id, "name", description, project_id, environment_id, product_version_id)
values (5, 'name5', 'description5', 1, 5, 5);

insert into tms_product_version (id, documentation, "version", project_id)
values (6, 'documentation6', 'version6', 1);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (6, 'milestone6', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type6', 6);

insert into tms_environment (id, "name", project_id)
values (6, 'name6', 1);

insert into tms_attribute (id, "key")
values (6, 'value6');

insert into tms_test_plan (id, "name", description, project_id, environment_id, product_version_id)
values (6, 'name6', 'description6', 1, 6, 6);

insert into tms_test_plan (id, "name", description, project_id)
values (100, 'Test Plan with Executions', 'Test plan that has test case executions', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (101, 'Test Plan without Executions', 'Test plan with no test case executions', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (102, 'Test Plan with Mixed Statuses', 'Test plan with mixed execution statuses', 1);

-- Additional test plans for duplication tests
insert into tms_test_plan (id, "name", description, project_id)
values (1, 'Test Plan 1', 'Description for test plan 1', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (2, 'Test Plan 2', 'Description for test plan 2', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (3, 'Test Plan 3', 'Description for test plan 3', 1);

-- Test folders for test cases
insert into tms_test_folder (id, "name", project_id)
values (7, 'Test Folder 1', 1),
       (8, 'Test Folder 2', 1),
       (9, 'Test Folder 3', 1);

-- Additional test folders for duplication tests
insert into tms_test_folder (id, "name", description, project_id)
values (4, 'Test Folder 4', 'Description for test folder 4', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (5, 'Test Folder 5', 'Description for test folder 5', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (6, 'Test Folder 6', 'Description for test folder 6', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (10, 'Test Folder 10', 'Description for test folder 10', 1);

-- Test cases for batch operations testing
insert into tms_test_case (id, "name", description, test_folder_id)
values (7, 'Test Case 7', 'Description for test case 7', 7),
       (8, 'Test Case 8', 'Description for test case 8', 7),
       (9, 'Test Case 9', 'Description for test case 9', 7),
       (10, 'Test Case 10', 'Description for test case 10', 8),
       (11, 'Test Case 11', 'Description for test case 11', 8),
       (12, 'Test Case 12', 'Description for test case 12', 8),
       (13, 'Test Case 13', 'Description for test case 13', 9),
       (14, 'Test Case 14', 'Description for test case 14', 9),
       (15, 'Test Case 15', 'Description for test case 15', 9),
       (16, 'Test Case 16', 'Description for test case 16', 7),
       (17, 'Test Case 17', 'Description for test case 17', 8),
       (18, 'Test Case 18', 'Description for test case 18', 9),
       (19, 'Test Case 19', 'Description for test case 19', 7),
       (20, 'Test Case 20', 'Description for test case 20', 8),
       (21, 'Test Case 21', 'Description for test case 21', 9);

-- Test cases for duplication functionality
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (4, 'Test Case 4', 'Description for test case 4', 4, 'HIGH'),
       (5, 'Test Case 5', 'Description for test case 5', 5, 'MEDIUM'),
       (6, 'Test Case 6', 'Description for test case 6', 6, 'LOW');

-- Test cases for execution statistics tests
insert into tms_test_case (id, "name", description, test_folder_id)
values (100, 'Test Case for Plan 100 - Covered', 'Test case with execution', 7),
       (101, 'Test Case for Plan 100 - Not Covered', 'Test case without execution', 7),
       (102, 'Test Case for Plan 101 - No Exec 1', 'Test case without execution 1', 7),
       (103, 'Test Case for Plan 101 - No Exec 2', 'Test case without execution 2', 7),
       (104, 'Test Case for Plan 102 - Passed', 'Test case with PASSED execution', 8),
       (105, 'Test Case for Plan 102 - Failed', 'Test case with FAILED execution', 8),
       (106, 'Test Case for Plan 102 - Skipped', 'Test case with SKIPPED execution', 8);

-- Test plan 100: 2 total, 1 covered (only test case 100 has execution)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (100, 100), (100, 101);

-- Test plan 101: 2 total, 0 covered (no test cases have executions)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (101, 102), (101, 103);

-- Test plan 102: 3 total, 2 covered (PASSED and FAILED count, SKIPPED doesn't)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (102, 104), (102, 105), (102, 106);

-- Test plan associations for duplication tests
-- Test Plan 1 contains test cases 4, 5, 6
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 4), (1, 5), (1, 6);

-- Test Plan 2 contains test cases 7, 8, 9
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (2, 7), (2, 8), (2, 9);

-- Test Plan 3 contains test cases 10, 11, 12
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (3, 10), (3, 11), (3, 12);

-- Additional attributes for duplication tests
insert into tms_attribute (id, "key")
values (1, 'test1');

insert into tms_attribute (id, "key")
values (2, 'test2');

insert into tms_attribute (id, "key")
values (7, 'priority');

insert into tms_attribute (id, "key")
values (8, 'environment');

-- Test plan attributes for duplication testing
insert into tms_test_plan_attribute (test_plan_id, attribute_id, value)
values (4, 4, 'test plan attribute value 4');

insert into tms_test_plan_attribute (test_plan_id, attribute_id, value)
values (5, 5, 'test plan attribute value 5');

insert into tms_test_plan_attribute (test_plan_id, attribute_id, value)
values (1, 1, 'original plan attribute 1');

insert into tms_test_plan_attribute (test_plan_id, attribute_id, value)
values (2, 2, 'original plan attribute 2');

-- Test case attributes for duplication testing
insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (4, 4, 'test case 4 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (5, 5, 'test case 5 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (6, 6, 'test case 6 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (7, 4, 'test case 7 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (8, 5, 'test case 8 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (9, 6, 'test case 9 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (10, 4, 'test case 10 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (11, 5, 'test case 11 attribute');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (12, 6, 'test case 12 attribute');

-- Test case versions for complete duplication testing
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (104, 4, 'Default Version 4', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (105, 5, 'Default Version 5', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (106, 6, 'Default Version 6', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (107, 7, 'Default Version 7', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (108, 8, 'Default Version 8', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (109, 9, 'Default Version 9', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (110, 10, 'Default Version 10', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (111, 11, 'Default Version 11', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (112, 12, 'Default Version 12', true, false);

-- Manual scenarios for test cases to ensure complete duplication
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (104, 104, 30, 'REQ-004', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (105, 105, 25, 'REQ-005', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (106, 106, 20, 'REQ-006', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (107, 107, 35, 'REQ-007', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (108, 108, 40, 'REQ-008', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (109, 109, 45, 'REQ-009', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (110, 110, 50, 'REQ-010', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (111, 111, 55, 'REQ-011', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (112, 112, 60, 'REQ-012', 'STEPS');

-- Text manual scenarios for TEXT type scenarios
insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (104, 'Execute test case 4 functionality', 'System should respond correctly for TC4');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (105, 'Execute test case 5 functionality', 'System should respond correctly for TC5');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (107, 'Execute test case 7 functionality', 'System should respond correctly for TC7');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (108, 'Execute test case 8 functionality', 'System should respond correctly for TC8');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (110, 'Execute test case 10 functionality', 'System should respond correctly for TC10');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (111, 'Execute test case 11 functionality', 'System should respond correctly for TC11');

-- Steps manual scenarios for STEPS type scenarios
insert into tms_steps_manual_scenario (manual_scenario_id)
values (106), (109), (112);

-- Steps for STEPS type scenarios (using correct column names: instructions, expected_result, steps_manual_scenario_id)
insert into tms_step (id, instructions, expected_result, steps_manual_scenario_id)
values (104, 'Step 1 for TC6: Initialize system', 'System should be ready', 106);

insert into tms_step (id, instructions, expected_result, steps_manual_scenario_id)
values (105, 'Step 2 for TC6: Execute action', 'Action should complete', 106);

insert into tms_step (id, instructions, expected_result, steps_manual_scenario_id)
values (106, 'Step 1 for TC9: Setup test environment', 'Environment should be ready', 109);

insert into tms_step (id, instructions, expected_result, steps_manual_scenario_id)
values (107, 'Step 2 for TC9: Run test scenario', 'Scenario should pass', 109);

insert into tms_step (id, instructions, expected_result, steps_manual_scenario_id)
values (108, 'Step 1 for TC12: Prepare test data', 'Data should be ready', 112);

insert into tms_step (id, instructions, expected_result, steps_manual_scenario_id)
values (109, 'Step 2 for TC12: Execute test', 'Test should complete', 112);

-- Manual scenario preconditions for complete testing
insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (104, 104, 'System must be initialized for TC4');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (105, 105, 'User must be authenticated for TC5');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (106, 106, 'Test environment ready for TC6');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (107, 107, 'Database initialized for TC7');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (108, 108, 'Network connectivity for TC8');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (109, 109, 'Test data prepared for TC9');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (110, 110, 'System configuration for TC10');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (111, 111, 'Application deployed for TC11');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (112, 112, 'Test environment ready for TC12');

-- Launch data for execution statistics testing
insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (200, '550e8400-e29b-41d4-a716-446655440200', 1, 1, 'Execution Stats Launch 1', 'Launch for testing execution statistics', '2023-10-10 10:00:00.000000', '2023-10-10 11:00:00.000000', 200, '2023-10-10 11:00:00.000000', 'DEFAULT', 'PASSED', false, false, 0, 'REGULAR', 100);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (201, '550e8400-e29b-41d4-a716-446655440201', 1, 1, 'Execution Stats Launch 2', 'Launch for testing mixed execution statuses', '2023-10-11 10:00:00.000000', '2023-10-11 11:00:00.000000', 201, '2023-10-11 11:00:00.000000', 'DEFAULT', 'FAILED', false, false, 0, 'REGULAR', 102);

-- Test items for executions
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (2000, '550e8400-e29b-41d4-a716-446655442000', 'Execution Stats Test Item 100', 'com.test.ExecutionStats100', 'TEST', '2023-10-10 10:30:00.000000', 'Test execution for statistics test case 100', '2023-10-10 10:35:00.000000', '2000', 'exec-stats-tc-100', null, false, false, true, null, null, 200, 2839500);

-- Test items for test plan 102 with mixed statuses
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (2040, '550e8400-e29b-41d4-a716-446655442040', 'Mixed Status Test Item 104', 'com.test.MixedStatus104', 'TEST', '2023-10-11 10:30:00.000000', 'Test execution with PASSED status', '2023-10-11 10:35:00.000000', '2040', 'mixed-status-tc-104', null, false, false, true, null, null, 201, 2839501);

insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (2050, '550e8400-e29b-41d4-a716-446655442050', 'Mixed Status Test Item 105', 'com.test.MixedStatus105', 'TEST', '2023-10-11 10:40:00.000000', 'Test execution with FAILED status', '2023-10-11 10:45:00.000000', '2050', 'mixed-status-tc-105', null, false, false, true, null, null, 201, 2839502);

insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (2060, '550e8400-e29b-41d4-a716-446655442060', 'Mixed Status Test Item 106', 'com.test.MixedStatus106', 'TEST', '2023-10-11 10:50:00.000000', 'Test execution with SKIPPED status', '2023-10-11 10:55:00.000000', '2060', 'mixed-status-tc-106', null, false, false, true, null, null, 201, 2839503);

-- Test item results
insert into test_item_results (result_id, status, end_time, duration)
values (2000, 'PASSED', '2023-10-10 10:35:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (2040, 'PASSED', '2023-10-11 10:35:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (2050, 'FAILED', '2023-10-11 10:45:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (2060, 'SKIPPED', '2023-10-11 10:55:00.000000', 300.0);

-- TMS Test Case Executions for execution statistics
insert into tms_test_case_execution (id, test_case_id, test_item_id, test_case_snapshot)
values (10, 100, 2000, '{"id": 100, "name": "Test Case for Plan 100 - Covered"}');

insert into tms_test_case_execution (id, test_case_id, test_item_id, test_case_snapshot)
values (11, 104, 2040, '{"id": 104, "name": "Test Case for Plan 102 - Passed"}');

insert into tms_test_case_execution (id, test_case_id, test_item_id, test_case_snapshot)
values (12, 105, 2050, '{"id": 105, "name": "Test Case for Plan 102 - Failed"}');

insert into tms_test_case_execution (id, test_case_id, test_item_id, test_case_snapshot)
values (13, 106, 2060, '{"id": 106, "name": "Test Case for Plan 102 - Skipped"}');

-- Update sequences for new IDs
SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_case_execution_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_execution));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_manual_scenario_preconditions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario_preconditions));
SELECT setval('tms_step_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_step));
SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
