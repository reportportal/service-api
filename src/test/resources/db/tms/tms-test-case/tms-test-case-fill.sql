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

-- Test cases
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (4, 'Test Case 4', 'Description for test case 4', 4, '1');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (5, 'Test Case 5', 'Description for test case 5', 5, '2');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (6, 'Test Case 6', 'Description for test case 6', 6, '3');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (7, 'Test Case 7', 'Description for test case 7', 4, '3');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (8, 'Test Case 8', 'Description for test case 8', 5, '3');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (9, 'Test Case 9', 'Description for test case 9', 4, '3');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (10, 'Test Case 10', 'Description for test case 10', 5, '1');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (11, 'Test Case 11', 'Description for test case 11', 5, '2');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (12, 'Test Case 12', 'Description for test case 12', 5, '3');

-- Search test cases
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (13, 'Search Test Case', 'This is a searchable test case', 7, '1');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (14, 'Another Test', 'Another description for search', 7, '2');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (15, 'Login Test Case', 'Test case for login functionality', 7, '1');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (16, 'API Test Case', 'Test case for API testing', 7, '2');

-- Test cases with versions and manual scenarios
insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (17, 'Test Case With Version', 'Test case with default version', 6, '1');

insert into tms_test_case (id, "name", description, test_folder_id, priority)
values (18, 'Complex Test Case', 'Test case with complex scenario', 6, '2');

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

-- Attributes
insert into tms_attribute (id, "key")
values (1, 'test1');

insert into tms_attribute (id, "key")
values (2, 'test2');

insert into tms_attribute (id, "key")
values (3, 'test3');

insert into tms_attribute (id, "key")
values (4, 'test4');

insert into tms_attribute (id, "key")
values (5, 'test5');

insert into tms_attribute (id, "key")
values (6, 'test6');

insert into tms_attribute (id, "key")
values (7, 'priority');

insert into tms_attribute (id, "key")
values (8, 'environment');

-- Test case attributes
insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (4, 4, 'test value 4');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (5, 5, 'test value 5');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (7, 4, 'test value 7');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (8, 5, 'test value 8');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (9, 4, 'test value 9');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (10, 5, 'test value 10');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (13, 7, 'high');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (15, 8, 'production');

-- Additional test case attributes for batch patch tests
insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (22, 1, 'existing-tag-22');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (23, 2, 'existing-tag-23');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (24, 3, 'existing-tag-24');

insert into tms_test_case_attribute (test_case_id, attribute_id, value)
values (25, 1, 'existing-tag-25');

-- Test case versions
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (1, 17, 'Default Version', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (2, 18, 'Version 1.0', true, false);

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (3, 18, 'Version 2.0', false, true);

-- Manual scenarios
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions)
values (1, 1, 30, 'REQ-001', 'User must be logged in');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions)
values (2, 2, 45, 'REQ-002', 'System must be initialized');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions)
values (3, 3, 60, 'REQ-003', 'Test environment ready');

-- Manual scenario attributes
insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (1, 7, 'critical');

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (2, 8, 'staging');

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (3, 7, 'medium');

-- Steps
insert into tms_step (id, manual_scenario_id, expected_result)
values (1, 1, 'Login form should appear');

insert into tms_step (id, manual_scenario_id, expected_result)
values (2, 2,'Username field should be filled');

insert into tms_step (id, manual_scenario_id, expected_result)
values (3, 2, 'Password field should be filled');

insert into tms_step (id, manual_scenario_id, expected_result)
values (4, 2, 'User should be logged in');

insert into tms_step (id, manual_scenario_id, expected_result)
values (5, 3, 'Application should start');

insert into tms_step (id, manual_scenario_id, expected_result)
values (6, 3, 'Settings page should open');

-- Set sequences to continue from highest ID
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_step_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_step));
