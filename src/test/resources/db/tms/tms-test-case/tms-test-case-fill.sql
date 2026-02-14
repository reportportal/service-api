-- Test folders
insert into tms_test_folder (id, "name", description, project_id)
values (3, 'Test Folder 3', 'Description for test folder 3', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (4, 'Test Folder 4', 'Description for test folder 4', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (5, 'Test Folder 5', 'Description for test folder 5', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (6, 'Test Folder 6', 'Description for test folder 6', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (7, 'Search Folder', 'Folder for search tests', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (8, 'Search Folder', 'Folder for full-text search tests', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (9, 'Test Folder 9', 'Description for test folder 9', 2);

insert into tms_test_folder (id, "name", description, project_id)
values (10, 'Test Folder 10', 'Description for test folder 10', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (11, 'Test Folder 11', 'Description for test folder 11', 2);

-- Test plans
insert into tms_test_plan (id, "name", description, project_id)
values (1, 'Test Plan 1', 'Description for test plan 1', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (2, 'Test Plan 2', 'Description for test plan 2', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (3, 'Test Plan 3', 'Description for test plan 3', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (4, 'Integration Test Plan', 'Test plan for integration tests', 1);

insert into tms_test_plan (id, "name", description, project_id)
values (5, 'Regression Test Plan', 'Test plan for regression tests', 1);

-- Test cases
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (4, 'Test Case 4', 'Description for test case 4', 4, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (5, 'Test Case 5', 'Description for test case 5', 5, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (6, 'Test Case 6', 'Description for test case 6', 6, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (7, 'Test Case 7', 'Description for test case 7', 4, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (8, 'Test Case 8', 'Description for test case 8', 5, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (9, 'Test Case 9', 'Description for test case 9', 4, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (10, 'Test Case 10', 'Description for test case 10', 5, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (11, 'Test Case 11', 'Description for test case 11', 5, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (12, 'Test Case 12', 'Description for test case 12', 5, 'LOW');

-- Search test cases
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (13, 'Search Test Case', 'This is a searchable test case', 7, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (14, 'Another Test', 'Another description for search', 7, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (15, 'Login Test Case', 'Test case for login functionality', 7, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (16, 'API Test Case', 'Test case for API testing', 7, 'MEDIUM');

-- Test cases with versions and manual scenarios
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (17, 'Test Case With Version', 'Test case with default version', 6, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (18, 'Complex Test Case', 'Test case with complex scenario', 6, 'MEDIUM');

-- Test cases for full-text searching
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (19, '3test', 'Test for full-text search 1', 8, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (20, 'Test for full-text search 2', 'Test for full-text search 2', 8, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (21, '3test', 'Test for full-text search 3', 7, 'MEDIUM');

-- Additional test cases for batch patch testing
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (22, 'Batch Test Case 22', 'Test case for batch patch with priority', 4, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (23, 'Batch Test Case 23', 'Test case for batch patch with priority', 4, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (24, 'Batch Test Case 24', 'Test case for batch patch with tags only', 5, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (25, 'Batch Test Case 25', 'Test case for batch patch with tags only', 5, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (26, 'Batch Test Case 26', 'Test case for batch patch with null fields', 6, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (27, 'Batch Test Case 27', 'Test case for batch patch with null fields', 6, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (28, 'Batch Test Case 28', 'Test case for batch patch with invalid attribute', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (29, 'Batch Test Case 29', 'Test case for batch patch with invalid attribute', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (30, 'Batch Test Case 30', 'Test case for batch patch with full attributes', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (31, 'Batch Test Case 31', 'Test case for batch patch with full attributes', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (32, 'Batch Test Case 32', 'Test case for batch patch with non existing folder 32', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (33, 'Batch Test Case 33', 'Test case for batch patch with non existing folder 33', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (34, 'Test Case With Version 34', 'Test case with default version 34', 6, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (35, 'Test Case With Version 35', 'Test case with default version 35', 9, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (36, 'Test Case With Version 36', 'Test case with default version 36', 9, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (37, 'Test Case With Version 37', 'Test case with default version 37', 10, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (38, 'Batch Patch New Folder Test 38', 'Test case for batch patch with new folder', 3, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (39, 'Batch Patch New Folder Test 39', 'Test case for batch patch with new folder', 3, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (40, 'Batch Patch Nested Folder Test 40', 'Test case for batch patch with nested folder', 4, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (41, 'Batch Patch Nested Folder Test 41', 'Test case for batch patch with nested folder', 4, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (42, 'Batch Patch Folder Priority Test 42', 'Test case for batch patch with folder and priority', 5, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (43, 'Batch Patch Folder Priority Test 43', 'Test case for batch patch with folder and priority', 5, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (44, 'Batch Patch Validation Test 44', 'Test case for batch patch validation', 6, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (45, 'Batch Patch Validation Test 45', 'Test case for batch patch validation', 6, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (46, 'Batch Patch Non-Existent Parent Test 46', 'Test case for non-existent parent folder', 7, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (47, 'Batch Patch Non-Existent Parent Test 47', 'Test case for non-existent parent folder', 7, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (48, 'Batch Patch Empty Name Test 48', 'Test case for empty folder name validation', 8, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (49, 'Batch Patch Empty Name Test 49', 'Test case for empty folder name validation', 8, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (50, 'Batch Patch Single TC Test 50', 'Test case for single test case batch patch', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (51, 'Default Project Batch Test 51', 'Test case for default project batch patch', 9, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (52, 'Default Project Batch Test 52', 'Test case for default project batch patch', 9, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (100, 'Test Case with Last Execution', 'Test case that has execution data', 3, 'HIGH');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (101, 'Test Case with Multiple Executions', 'Test case with multiple executions, should return latest', 3, 'MEDIUM');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (102, 'Test Case without Execution', 'Test case with no execution data', 3, 'LOW');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (103, 'Test Case with Single Execution', 'Test case with single execution', 3, 'HIGH');

-- Test plan - test case relationships
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 4), (1, 5), (1, 6);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (2, 7), (2, 8), (2, 9);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (3, 10), (3, 11), (3, 12);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (4, 13), (4, 14), (4, 15);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (5, 16), (5, 17), (5, 18);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (4, 4), (4, 5);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 13), (2, 15);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 14), (1, 15), (1, 16);

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (2, 19), (3, 20);

-- Attributes (key and project_id, value is always NULL)
insert into tms_attribute (id, "key", project_id, value)
values (1, 'test1', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (2, 'test2', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (3, 'test3', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (4, 'test4', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (5, 'test5', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (6, 'test6', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (7, 'priority', 1, NULL);

insert into tms_attribute (id, "key", project_id, value)
values (8, 'environment', 1, NULL);

-- Test case attributes (no value field, just relationship)
insert into tms_test_case_attribute (test_case_id, attribute_id)
values (4, 4);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (5, 5);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (7, 4);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (8, 5);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (9, 4);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (10, 5);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (13, 7);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (15, 8);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (22, 1);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (23, 2);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (24, 3);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (25, 1);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (36, 1);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (37, 1);

insert into tms_test_case_attribute (test_case_id, attribute_id)
values (37, 2);

-- Test case versions
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (1, 4, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (2, 5, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (3, 6, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (4, 7, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (5, 8, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (6, 9, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (7, 10, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (8, 11, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (9, 12, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (10, 13, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (11, 14, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (12, 15, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (13, 16, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (14, 17, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (15, 18, 'Version 1.0', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (16, 18, 'Version 2.0', false, true);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (17, 19, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (18, 20, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (19, 21, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (20, 22, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (21, 23, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (22, 24, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (23, 25, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (24, 26, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (25, 27, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (26, 28, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (27, 29, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (28, 30, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (29, 31, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (30, 32, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (31, 33, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (32, 34, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (33, 35, 'Default Version 35', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (34, 36, 'Default Version 36', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (35, 37, 'Default Version 37', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (38, 38, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (39, 39, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (40, 40, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (41, 41, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (42, 42, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (43, 43, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (44, 44, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (45, 45, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (46, 46, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (47, 47, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (48, 48, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (49, 49, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (50, 50, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (51, 51, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (52, 52, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (100, 100, 'Default Version 100', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (101, 101, 'Default Version 101', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (102, 102, 'Default Version 102', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (103, 103, 'Default Version 103', true, false);

-- Manual scenarios
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (1, 1, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (2, 2, 25, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (3, 3, 20, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (4, 4, 35, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (5, 5, 40, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (6, 6, 15, 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (7, 7, 45, 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (8, 8, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (9, 9, 25, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (10, 10, 50, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (11, 11, 35, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (12, 12, 40, 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (13, 13, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (14, 14, 45, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (15, 15, 60, 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (16, 16, 35, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (17, 17, 20, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (18, 18, 25, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (19, 19, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (20, 20, 15, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (21, 21, 20, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (22, 22, 25, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (23, 23, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (24, 24, 20, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (25, 25, 25, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (26, 26, 35, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (27, 27, 40, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (28, 28, 45, 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (29, 29, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (30, 30, 25, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (31, 31, 30, 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
values (32, 32, 35, 'STEPS');

-- Manual scenario requirements
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-004', 'Requirement for TC4', 1, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-005', 'Requirement for TC5', 2, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-006', 'Requirement for TC6', 3, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-007', 'Requirement for TC7', 4, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-008', 'Requirement for TC8', 5, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-009', 'Requirement for TC9', 6, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-010', 'Requirement for TC10', 7, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-011', 'Requirement for TC11', 8, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-012', 'Requirement for TC12', 9, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-013', 'Requirement for TC13', 10, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-014', 'Requirement for TC14', 11, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-015', 'Requirement for TC15', 12, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-016', 'Requirement for TC16', 13, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-017', 'Requirement for TC17', 14, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-018', 'Requirement for TC18', 15, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-019', 'Requirement for TC19', 16, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-020', 'Requirement for TC20', 17, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-021', 'Requirement for TC21', 18, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-022', 'Requirement for TC22', 19, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-023', 'Requirement for TC23', 20, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-024', 'Requirement for TC24', 21, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-025', 'Requirement for TC25', 22, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-026', 'Requirement for TC26', 23, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-027', 'Requirement for TC27', 24, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-028', 'Requirement for TC28', 25, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-029', 'Requirement for TC29', 26, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-030', 'Requirement for TC30', 27, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-031', 'Requirement for TC31', 28, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-032', 'Requirement for TC32', 29, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-033', 'Requirement for TC33', 30, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-034', 'Requirement for TC34', 31, 0);
insert into tms_manual_scenario_requirement (id, value, manual_scenario_id, number) values ('REQ-035', 'Requirement for TC35', 32, 0);

-- Manual scenario preconditions
insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (1, 1, 'System must be ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (2, 2, 'User must be logged in');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (3, 3, 'Test environment ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (4, 4, 'System initialized');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (5, 5, 'User authentication ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (6, 6, 'Basic setup complete');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (7, 7, 'High priority test setup');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (8, 8, 'Medium complexity setup');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (9, 9, 'Low priority setup');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (10, 10, 'Search functionality ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (11, 11, 'Search system initialized');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (12, 12, 'Login system ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (13, 13, 'API endpoints available');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (14, 14, 'Version control ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (15, 15, 'Complex scenario setup');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (16, 16, 'Draft version setup');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (17, 17, 'Full-text search ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (18, 18, 'Search indexing complete');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (19, 19, 'Search filters ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (20, 20, 'Batch processing ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (21, 21, 'Priority batch ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (22, 22, 'Tag management ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (23, 23, 'Tag processing ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (24, 24, 'Null field handling ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (25, 25, 'Null validation ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (26, 26, 'Attribute validation ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (27, 27, 'Invalid attribute handling');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (28, 28, 'Full attribute processing');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (29, 29, 'Complete attribute setup');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (30, 30, 'Folder validation ready');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (31, 31, 'Non-existing folder handling');

insert into tms_manual_scenario_preconditions (id, manual_scenario_id, value)
values (32, 32, 'Final version processing');

-- Text manual scenarios
insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (1, 'Execute test case 4 functionality', 'Test should pass with high priority');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (2, 'Execute test case 5 functionality', 'Test should pass with medium priority');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (3, 'Execute test case 6 functionality', 'Test should pass with low priority');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (4, 'Execute test case 7 functionality', 'System should respond correctly');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (5, 'Execute test case 8 functionality', 'Authentication should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (8, 'Execute test case 11 functionality', 'Medium complexity test should pass');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (9, 'Execute test case 12 functionality', 'Low priority test should complete');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (10, 'Execute search test case functionality', 'Search should return correct results');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (11, 'Execute another test functionality', 'System should handle the test');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (13, 'Execute API test case functionality', 'API endpoints should respond');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (14, 'Execute version test functionality', 'Version control should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (16, 'Execute draft version functionality', 'Draft should be processed');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (17, 'Execute full-text search test', 'Search indexing should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (18, 'Execute search indexing test', 'Indexing should complete');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (19, 'Execute search filter test', 'Filters should apply correctly');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (20, 'Execute batch test 22', 'Batch processing should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (21, 'Execute batch test 23', 'Priority batch should process');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (22, 'Execute tag management test', 'Tags should be managed correctly');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (23, 'Execute tag processing test', 'Tag processing should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (24, 'Execute null field test', 'Null fields should be handled');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (25, 'Execute null validation test', 'Null validation should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (26, 'Execute attribute validation test', 'Attributes should be validated');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (27, 'Execute invalid attribute test', 'Invalid attributes should be handled');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (29, 'Execute complete attribute test', 'All attributes should process');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (30, 'Execute folder validation test', 'Folder validation should work');

insert into tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
values (31, 'Execute non-existing folder test', 'Non-existing folders should be handled');

-- Steps manual scenarios
insert into tms_steps_manual_scenario (manual_scenario_id)
values (6);

insert into tms_steps_manual_scenario (manual_scenario_id)
values (7);

insert into tms_steps_manual_scenario (manual_scenario_id)
values (12);

insert into tms_steps_manual_scenario (manual_scenario_id)
values (15);

insert into tms_steps_manual_scenario (manual_scenario_id)
values (28);

insert into tms_steps_manual_scenario (manual_scenario_id)
values (32);

-- Manual scenario attributes (no value field, just relationship)
insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id)
values (1, 7);

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id)
values (2, 8);

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id)
values (10, 7);

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id)
values (12, 8);

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id)
values (15, 7);

-- Steps
insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (1, 6, 'Navigate to test case 6', 'Application should start');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (2, 6, 'Execute basic functionality', 'Basic functions should work');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (3, 6, 'Verify results', 'Results should be correct');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (4, 7, 'Navigate to high priority test', 'High priority test should load');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (5, 7, 'Execute high priority actions', 'Actions should complete');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (6, 7, 'Verify high priority results', 'Results should meet criteria');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (7, 12, 'Navigate to login page', 'Login page should appear');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (8, 12, 'Enter credentials', 'Credentials should be accepted');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (9, 12, 'Verify login success', 'User should be logged in');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (10, 15, 'Navigate to complex scenario', 'Complex scenario should load');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (11, 15, 'Execute complex operations', 'Operations should complete');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (12, 15, 'Verify complex results', 'All results should be correct');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (13, 28, 'Navigate to attribute processing', 'Attribute processing should start');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (14, 28, 'Execute full attribute test', 'All attributes should be processed');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (15, 28, 'Verify attribute results', 'Attribute processing should succeed');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (16, 32, 'Navigate to final version test', 'Final version should load');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (17, 32, 'Execute final processing', 'Final processing should complete');

insert into tms_step (id, steps_manual_scenario_id, instructions, expected_result)
values (18, 32, 'Verify final results', 'Final results should be correct');

-- Launches for execution tests
insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (100, '550e8400-e29b-41d4-a716-446655440100', 1, 1, 'Launch for TC Execution Tests', 'Launch for testing last execution functionality', '2023-10-06 10:00:00.000000', '2023-10-06 11:00:00.000000', 100, '2023-10-06 11:00:00.000000', 'DEFAULT', 'PASSED', false, false, 0, 'REGULAR', null);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (101, '550e8400-e29b-41d4-a716-446655440101', 1, 1, 'Launch for Multiple Executions', 'Launch for testing multiple executions', '2023-10-07 10:00:00.000000', '2023-10-07 11:00:00.000000', 101, '2023-10-07 11:00:00.000000', 'DEFAULT', 'PASSED', false, false, 0, 'REGULAR', null);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, retention_policy, test_plan_id)
values (102, '550e8400-e29b-41d4-a716-446655440102', 1, 1, 'Launch for Latest Execution', 'Launch for testing latest execution logic', '2023-10-08 10:00:00.000000', '2023-10-08 11:00:00.000000', 102, '2023-10-08 11:00:00.000000', 'DEFAULT', 'PASSED', false, false, 0, 'REGULAR', null);

-- Test items for execution tests
insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (1000, '550e8400-e29b-41d4-a716-446655441000', 'Test Item for TC 100', 'com.test.TC100', 'TEST', '2023-10-06 10:00:00.000000', 'Test execution for test case 100', '2023-10-06 10:05:00.000000', '1000', 'tc-100-exec-1', null, false, false, true, null, null, 100, 2839437);

insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (1010, '550e8400-e29b-41d4-a716-446655441010', 'Test Item for TC 101 - First', 'com.test.TC101', 'TEST', '2023-10-05 10:00:00.000000', 'First execution for test case 101', '2023-10-05 10:05:00.000000', '1010', 'tc-101-exec-1', null, false, false, true, null, null, 100, 2839438);

insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (1011, '550e8400-e29b-41d4-a716-446655441011', 'Test Item for TC 101 - Latest', 'com.test.TC101', 'TEST', '2023-10-07 14:00:00.000000', 'Latest execution for test case 101', '2023-10-07 14:05:00.000000', '1011', 'tc-101-exec-2', null, false, false, true, null, null, 101, 2839439);

insert into test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
values (1030, '550e8400-e29b-41d4-a716-446655441030', 'Test Item for TC 103', 'com.test.TC103', 'TEST', '2023-10-08 15:00:00.000000', 'Test execution for test case 103', '2023-10-08 15:05:00.000000', '1030', 'tc-103-exec-1', null, false, false, true, null, null, 102, 2839440);

-- Test case executions
insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (1, 100, 100, 1000, '{"id": 100, "name": "Test Case with Last Execution", "priority": "HIGH"}', 100);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (2, 101, 101, 1010, '{"id": 101, "name": "Test Case with Multiple Executions", "priority": "MEDIUM"}', 101);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (3, 101, 101, 1011, '{"id": 101, "name": "Test Case with Multiple Executions", "priority": "MEDIUM"}', 101);

insert into tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
values (4, 103, 103, 1030, '{"id": 103, "name": "Test Case with Single Execution", "priority": "HIGH"}', 102);

-- Set sequences to continue from highest ID
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_manual_scenario_preconditions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario_preconditions));
SELECT setval('tms_step_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_step));
SELECT setval('tms_test_case_execution_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_execution));