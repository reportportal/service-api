insert into tms_product_version (id, documentation, "version", project_id)
values (3, 'documentation3', 'version3', 1);

insert into tms_environment (id, "name", project_id)
values (3, 'name3', 1);

insert into tms_attribute (id, "key")
values (3, 'value3');

insert into tms_product_version (id, documentation, "version", project_id)
values (4, 'documentation4', 'version4', 1);

insert into tms_environment (id, "name", project_id)
values (4, 'name4', 1);

insert into tms_attribute (id, "key")
values (4, 'value4');

insert into tms_test_plan (id, "name", description, project_id, environment_id, product_version_id)
values (4, 'name4', 'description4', 1, 4, 4);

insert into tms_product_version (id, documentation, "version", project_id)
values (5, 'documentation5', 'version5', 1);

insert into tms_environment (id, "name", project_id)
values (5, 'name5', 1);

insert into tms_attribute (id, "key")
values (5, 'value5');

insert into tms_test_plan (id, "name", description, project_id, environment_id, product_version_id)
values (5, 'name5', 'description5', 1, 5, 5);

insert into tms_product_version (id, documentation, "version", project_id)
values (6, 'documentation6', 'version6', 1);

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

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (100, 100, 'Default Version 100', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (101, 101, 'Default Version 101', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (102, 102, 'Default Version 102', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (103, 103, 'Default Version 103', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (104, 104, 'Default Version 104', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (105, 105, 'Default Version 105', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (106, 106, 'Default Version 106', true, false);

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
values (4, 4, 'Default Version 4', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (5, 5, 'Default Version 5', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (6, 6, 'Default Version 6', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (7, 7, 'Default Version 7', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (8, 8, 'Default Version 8', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (9, 9, 'Default Version 9', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (10, 10, 'Default Version 10', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (11, 11, 'Default Version 11', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (12, 12, 'Default Version 12', true, false);

-- Manual scenarios for test cases to ensure complete duplication
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (104, 4, 30, 'REQ-004', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (105, 5, 25, 'REQ-005', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (106, 6, 20, 'REQ-006', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (107, 7, 35, 'REQ-007', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (108, 8, 40, 'REQ-008', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (109, 9, 45, 'REQ-009', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (110, 10, 50, 'REQ-010', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (111, 11, 55, 'REQ-011', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (112, 12, 60, 'REQ-012', 'STEPS');

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
insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (10, 100, 100, 2000, '{"id": 100, "name": "Test Case for Plan 100 - Covered"}', 200);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (11, 104, 104, 2040, '{"id": 104, "name": "Test Case for Plan 102 - Passed"}', 201);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (12, 105, 105, 2050, '{"id": 105, "name": "Test Case for Plan 102 - Failed"}', 201);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (13, 106, 106, 2060, '{"id": 106, "name": "Test Case for Plan 102 - Skipped"}', 201);

-- =====================================================
-- Test data for getTestCasesAddedToPlan and getTestCaseInTestPlan endpoints
-- =====================================================

-- Test plans for testing test case retrieval endpoints
insert into tms_test_plan (id, "name", description, project_id)
values (200, 'Test Plan for Test Cases Retrieval', 'Test plan with test cases and executions', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (201, 'Empty Test Plan for Retrieval', 'Test plan without any test cases', 1);

-- Test folders for test cases
insert into tms_test_folder (id, "name", description, project_id)
values (200, 'Test Folder for Retrieval Tests', 'Folder for test case retrieval tests', 1);

-- Test cases for test plan 200
-- Test case 200: will have 3 executions (to test ordering)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (200, 'Test Case with Multiple Executions', 'Test case with 3 executions', 200, 'HIGH', 'TC-200');

-- Test case 201: will have 2 executions
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (201, 'Test Case with Two Executions', 'Test case with 2 executions', 200, 'MEDIUM', 'TC-201');

-- Test case 202: will have 1 execution
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (202, 'Test Case with One Execution', 'Test case with 1 execution', 200, 'LOW', 'TC-202');

-- Test case 203: will have 0 executions
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (203, 'Test Case without Executions', 'Test case with no executions', 200, 'MEDIUM', 'TC-203');

-- Test case 204: not in test plan (for 404 tests)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (204, 'Test Case Not in Plan', 'Test case not added to any plan', 200, 'HIGH', 'TC-204');

-- Add test cases to test plan 200 (except test case 204)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (200, 200), (200, 201), (200, 202), (200, 203);

-- Test case versions for complete testing
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (200, 200, 'Default Version 200', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (201, 201, 'Default Version 201', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (202, 202, 'Default Version 202', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (203, 203, 'Default Version 203', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (204, 204, 'Default Version 204', true, false);

-- Manual scenarios for test cases
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (200, 200, 30, 'REQ-200', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (201, 201, 25, 'REQ-201', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (202, 202, 20, 'REQ-202', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (203, 203, 15, 'REQ-203', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (204, 204, 10, 'REQ-204', 'TEXT');

-- Text manual scenarios
insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (200, 'Execute test case 200', 'System should respond correctly for TC200');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (201, 'Execute test case 201', 'System should respond correctly for TC201');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (202, 'Execute test case 202', 'System should respond correctly for TC202');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (203, 'Execute test case 203', 'System should respond correctly for TC203');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (204, 'Execute test case 204', 'System should respond correctly for TC204');

-- Launches for test case executions
insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (300, '550e8400-e29b-41d4-a716-446655440300', 1, 1, 'Launch 300 for Test Cases', 'Launch for test case retrieval tests', '2023-12-01 10:00:00.000000', '2023-12-01 11:00:00.000000', 300, '2023-12-01 11:00:00.000000', 'DEFAULT', 'PASSED', false, false, 0, 'REGULAR', 200);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (301, '550e8400-e29b-41d4-a716-446655440301', 1, 1, 'Launch 301 for Test Cases', 'Second launch for test case retrieval tests', '2023-12-02 10:00:00.000000', '2023-12-02 11:00:00.000000', 301, '2023-12-02 11:00:00.000000', 'DEFAULT', 'FAILED', false, false, 0, 'REGULAR', 200);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (302, '550e8400-e29b-41d4-a716-446655440302', 1, 1, 'Launch 302 for Test Cases', 'Third launch for test case retrieval tests', '2023-12-03 10:00:00.000000', '2023-12-03 11:00:00.000000', 302, '2023-12-03 11:00:00.000000', 'DEFAULT', 'PASSED', false, false, 0, 'REGULAR', 200);

-- Test items for test case 200 (3 executions - ordered by start_time DESC in queries)
-- Execution 1 (oldest)
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (3000, '550e8400-e29b-41d4-a716-446655443000', 'Test Item 200-1', 'com.test.TestCase200_1', 'TEST', '2023-12-01 10:30:00.000000', 'First execution of test case 200', '2023-12-01 10:35:00.000000', '3000', 'tc-200-exec-1', null, false, false, true, null, null, 300, 3000);

-- Execution 2 (middle)
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (3001, '550e8400-e29b-41d4-a716-446655443001', 'Test Item 200-2', 'com.test.TestCase200_2', 'TEST', '2023-12-02 10:30:00.000000', 'Second execution of test case 200', '2023-12-02 10:35:00.000000', '3001', 'tc-200-exec-2', null, false, false, true, null, null, 301, 3001);

-- Execution 3 (latest/newest)
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (3002, '550e8400-e29b-41d4-a716-446655443002', 'Test Item 200-3', 'com.test.TestCase200_3', 'TEST', '2023-12-03 10:30:00.000000', 'Third (latest) execution of test case 200', '2023-12-03 10:35:00.000000', '3002', 'tc-200-exec-3', null, false, false, true, null, null, 302, 3002);

-- Test items for test case 201 (2 executions)
-- Execution 1 (older)
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (3010, '550e8400-e29b-41d4-a716-446655443010', 'Test Item 201-1', 'com.test.TestCase201_1', 'TEST', '2023-12-01 11:00:00.000000', 'First execution of test case 201', '2023-12-01 11:05:00.000000', '3010', 'tc-201-exec-1', null, false, false, true, null, null, 300, 3010);

-- Execution 2 (latest)
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (3011, '550e8400-e29b-41d4-a716-446655443011', 'Test Item 201-2', 'com.test.TestCase201_2', 'TEST', '2023-12-02 11:00:00.000000', 'Second (latest) execution of test case 201', '2023-12-02 11:05:00.000000', '3011', 'tc-201-exec-2', null, false, false, true, null, null, 301, 3011);

-- Test items for test case 202 (1 execution)
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (3020, '550e8400-e29b-41d4-a716-446655443020', 'Test Item 202-1', 'com.test.TestCase202_1', 'TEST', '2023-12-01 12:00:00.000000', 'Only execution of test case 202', '2023-12-01 12:05:00.000000', '3020', 'tc-202-exec-1', null, false, false, true, null, null, 300, 3020);

-- Test item results
insert into test_item_results (result_id, status, end_time, duration)
values (3000, 'PASSED', '2023-12-01 10:35:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (3001, 'FAILED', '2023-12-02 10:35:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (3002, 'PASSED', '2023-12-03 10:35:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (3010, 'PASSED', '2023-12-01 11:05:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (3011, 'FAILED', '2023-12-02 11:05:00.000000', 300.0);

insert into test_item_results (result_id, status, end_time, duration)
values (3020, 'PASSED', '2023-12-01 12:05:00.000000', 300.0);

-- TMS Test Case Executions
-- Test case 200 executions
insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (200, 200, 200, 3000, '{"id": 200, "name": "Test Case with Multiple Executions"}', 300);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (201, 200, 200,  3001, '{"id": 200, "name": "Test Case with Multiple Executions"}', 301);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (202, 200, 200, 3002, '{"id": 200, "name": "Test Case with Multiple Executions"}', 300);

-- Test case 201 executions
insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (210, 201, 201, 3010, '{"id": 201, "name": "Test Case with Two Executions"}', 301);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (211, 201, 201, 3011, '{"id": 201, "name": "Test Case with Two Executions"}', 301);

-- Test case 202 execution
insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (220, 202, 202, 3020, '{"id": 202, "name": "Test Case with One Execution"}', 300);

-- =====================================================
-- Test data for getTestFoldersFromPlan endpoint
-- =====================================================

-- Test plans for folder retrieval tests
insert into tms_test_plan (id, "name", description, project_id)
values (300, 'Test Plan 300 - Multiple Folders', 'Plan with test cases from multiple folders', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (301, 'Test Plan 301 - Empty', 'Plan without test cases', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (302, 'Test Plan 302 - Single Folder', 'Plan with test cases from single folder', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (303, 'Test Plan 303 - Many Folders', 'Plan with test cases from many folders for pagination', 1);

-- Test folders for folder retrieval tests
insert into tms_test_folder (id, "name", description, project_id)
values (300, 'Folder 300 for Plan 300', 'First folder with test cases in plan 300', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (301, 'Folder 301 for Plan 300', 'Second folder with test cases in plan 300', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (302, 'Folder 302 for Plan 300', 'Third folder with test cases in plan 300', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (303, 'Folder 303 for Plan 302', 'Single folder with multiple test cases', 1);

-- Folders 304-313 for pagination test (10 folders)
insert into tms_test_folder (id, "name", description, project_id)
values (304, 'Folder 304', 'Folder for pagination 1', 1),
       (305, 'Folder 305', 'Folder for pagination 2', 1),
       (306, 'Folder 306', 'Folder for pagination 3', 1),
       (307, 'Folder 307', 'Folder for pagination 4', 1),
       (308, 'Folder 308', 'Folder for pagination 5', 1),
       (309, 'Folder 309', 'Folder for pagination 6', 1),
       (310, 'Folder 310', 'Folder for pagination 7', 1),
       (311, 'Folder 311', 'Folder for pagination 8', 1),
       (312, 'Folder 312', 'Folder for pagination 9', 1),
       (313, 'Folder 313', 'Folder for pagination 10', 1);

-- Test cases for folder 300 (2 test cases)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (300, 'TC 300 in Folder 300', 'Test case in folder 300', 300, 'HIGH', 'TC-300'),
       (301, 'TC 301 in Folder 300', 'Test case in folder 300', 300, 'MEDIUM', 'TC-301');

-- Test cases for folder 301 (3 test cases)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (302, 'TC 302 in Folder 301', 'Test case in folder 301', 301, 'HIGH', 'TC-302'),
       (303, 'TC 303 in Folder 301', 'Test case in folder 301', 301, 'LOW', 'TC-303'),
       (304, 'TC 304 in Folder 301', 'Test case in folder 301', 301, 'MEDIUM', 'TC-304');

-- Test cases for folder 302 (1 test case)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (305, 'TC 305 in Folder 302', 'Test case in folder 302', 302, 'HIGH', 'TC-305');

-- Test cases for folder 303 (5 test cases for single folder test)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (306, 'TC 306 in Folder 303', 'Test case in folder 303', 303, 'HIGH', 'TC-306'),
       (307, 'TC 307 in Folder 303', 'Test case in folder 303', 303, 'MEDIUM', 'TC-307'),
       (308, 'TC 308 in Folder 303', 'Test case in folder 303', 303, 'LOW', 'TC-308'),
       (309, 'TC 309 in Folder 303', 'Test case in folder 303', 303, 'HIGH', 'TC-309'),
       (310, 'TC 310 in Folder 303', 'Test case in folder 303', 303, 'MEDIUM', 'TC-310');

-- Test cases for pagination folders (one test case per folder)
insert into tms_test_case (id, "name", description, test_folder_id, priority, external_id)
values (311, 'TC in Folder 304', 'Test case for pagination', 304, 'HIGH', 'TC-311'),
       (312, 'TC in Folder 305', 'Test case for pagination', 305, 'HIGH', 'TC-312'),
       (313, 'TC in Folder 306', 'Test case for pagination', 306, 'HIGH', 'TC-313'),
       (314, 'TC in Folder 307', 'Test case for pagination', 307, 'HIGH', 'TC-314'),
       (315, 'TC in Folder 308', 'Test case for pagination', 308, 'HIGH', 'TC-315'),
       (316, 'TC in Folder 309', 'Test case for pagination', 309, 'HIGH', 'TC-316'),
       (317, 'TC in Folder 310', 'Test case for pagination', 310, 'HIGH', 'TC-317'),
       (318, 'TC in Folder 311', 'Test case for pagination', 311, 'HIGH', 'TC-318'),
       (319, 'TC in Folder 312', 'Test case for pagination', 312, 'HIGH', 'TC-319'),
       (320, 'TC in Folder 313', 'Test case for pagination', 313, 'HIGH', 'TC-320');

-- Add test cases to test plan 300 (from folders 300, 301, 302)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (300, 300), (300, 301),  -- 2 from folder 300
       (300, 302), (300, 303), (300, 304),  -- 3 from folder 301
       (300, 305);  -- 1 from folder 302

-- Test plan 301 has no test cases (empty)

-- Add test cases to test plan 302 (all from folder 303)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (302, 306), (302, 307), (302, 308), (302, 309), (302, 310);

-- Add test cases to test plan 303 (for pagination - 10 folders)
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (303, 311), (303, 312), (303, 313), (303, 314), (303, 315),
       (303, 316), (303, 317), (303, 318), (303, 319), (303, 320);

-- Test case versions for folders test data
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (300, 300, 'Default Version 300', true, false),
       (301, 301, 'Default Version 301', true, false),
       (302, 302, 'Default Version 302', true, false),
       (303, 303, 'Default Version 303', true, false),
       (304, 304, 'Default Version 304', true, false),
       (305, 305, 'Default Version 305', true, false),
       (306, 306, 'Default Version 306', true, false),
       (307, 307, 'Default Version 307', true, false),
       (308, 308, 'Default Version 308', true, false),
       (309, 309, 'Default Version 309', true, false),
       (310, 310, 'Default Version 310', true, false),
       (311, 311, 'Default Version 311', true, false),
       (312, 312, 'Default Version 312', true, false),
       (313, 313, 'Default Version 313', true, false),
       (314, 314, 'Default Version 314', true, false),
       (315, 315, 'Default Version 315', true, false),
       (316, 316, 'Default Version 316', true, false),
       (317, 317, 'Default Version 317', true, false),
       (318, 318, 'Default Version 318', true, false),
       (319, 319, 'Default Version 319', true, false),
       (320, 320, 'Default Version 320', true, false);

-- Manual scenarios for folders test data
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
values (300, 300, 30, 'REQ-300', 'TEXT'),
       (301, 301, 25, 'REQ-301', 'TEXT'),
       (302, 302, 20, 'REQ-302', 'TEXT'),
       (303, 303, 15, 'REQ-303', 'TEXT'),
       (304, 304, 30, 'REQ-304', 'TEXT'),
       (305, 305, 25, 'REQ-305', 'TEXT'),
       (306, 306, 20, 'REQ-306', 'TEXT'),
       (307, 307, 15, 'REQ-307', 'TEXT'),
       (308, 308, 30, 'REQ-308', 'TEXT'),
       (309, 309, 25, 'REQ-309', 'TEXT'),
       (310, 310, 20, 'REQ-310', 'TEXT'),
       (311, 311, 15, 'REQ-311', 'TEXT'),
       (312, 312, 30, 'REQ-312', 'TEXT'),
       (313, 313, 25, 'REQ-313', 'TEXT'),
       (314, 314, 20, 'REQ-314', 'TEXT'),
       (315, 315, 15, 'REQ-315', 'TEXT'),
       (316, 316, 30, 'REQ-316', 'TEXT'),
       (317, 317, 25, 'REQ-317', 'TEXT'),
       (318, 318, 20, 'REQ-318', 'TEXT'),
       (319, 319, 15, 'REQ-319', 'TEXT'),
       (320, 320, 30, 'REQ-320', 'TEXT');

-- Text manual scenarios
insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (300, 'Execute test case 300', 'System should respond correctly for TC300'),
       (301, 'Execute test case 301', 'System should respond correctly for TC301'),
       (302, 'Execute test case 302', 'System should respond correctly for TC302'),
       (303, 'Execute test case 303', 'System should respond correctly for TC303'),
       (304, 'Execute test case 304', 'System should respond correctly for TC304'),
       (305, 'Execute test case 305', 'System should respond correctly for TC305'),
       (306, 'Execute test case 306', 'System should respond correctly for TC306'),
       (307, 'Execute test case 307', 'System should respond correctly for TC307'),
       (308, 'Execute test case 308', 'System should respond correctly for TC308'),
       (309, 'Execute test case 309', 'System should respond correctly for TC309'),
       (310, 'Execute test case 310', 'System should respond correctly for TC310'),
       (311, 'Execute test case 311', 'System should respond correctly for TC311'),
       (312, 'Execute test case 312', 'System should respond correctly for TC312'),
       (313, 'Execute test case 313', 'System should respond correctly for TC313'),
       (314, 'Execute test case 314', 'System should respond correctly for TC314'),
       (315, 'Execute test case 315', 'System should respond correctly for TC315'),
       (316, 'Execute test case 316', 'System should respond correctly for TC316'),
       (317, 'Execute test case 317', 'System should respond correctly for TC317'),
       (318, 'Execute test case 318', 'System should respond correctly for TC318'),
       (319, 'Execute test case 319', 'System should respond correctly for TC319'),
       (320, 'Execute test case 320', 'System should respond correctly for TC320');

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
SELECT setval('launch_id_seq', (SELECT COALESCE(MAX(id), 1) FROM launch));
SELECT setval('test_item_item_id_seq', (SELECT COALESCE(MAX(item_id), 1) FROM test_item));
