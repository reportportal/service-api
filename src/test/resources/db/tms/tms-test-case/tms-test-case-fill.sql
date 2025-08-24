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

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 4), (1, 5), (1, 6); -- Test Plan 1 contains test cases 4, 5, 6

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (2, 7), (2, 8), (2, 9); -- Test Plan 2 contains test cases 7, 8, 9

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (3, 10), (3, 11), (3, 12); -- Test Plan 3 contains test cases 10, 11, 12

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (4, 13), (4, 14), (4, 15); -- Integration Test Plan contains test cases 13, 14, 15

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (5, 16), (5, 17), (5, 18); -- Regression Test Plan contains test cases 16, 17, 18

-- Some test cases in multiple test plans
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (4, 4), (4, 5); -- Test cases 4, 5 also in Integration Test Plan

insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 13), (2, 15); -- Cross-plan assignments for testing

-- Test cases for test plan filtering in folder 7
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (1, 14), (1, 15), (1, 16); -- Additional folder 7 test cases in Test Plan 1

-- Test cases for folder 8 in different test plans
insert into tms_test_plan_test_case (test_plan_id, test_case_id)
values (2, 19), (3, 20); -- Folder 8 test cases in different plans

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

-- Test case versions (Default version for each test case)
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

-- Manual scenarios for each version
-- TEXT scenarios for most cases
insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (1, 1, 30, 'REQ-004', 'System must be ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (2, 2, 25, 'REQ-005', 'User must be logged in', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (3, 3, 20, 'REQ-006', 'Test environment ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (4, 4, 35, 'REQ-007', 'System initialized', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (5, 5, 40, 'REQ-008', 'User authentication ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (6, 6, 15, 'REQ-009', 'Basic setup complete', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (7, 7, 45, 'REQ-010', 'High priority test setup', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (8, 8, 30, 'REQ-011', 'Medium complexity setup', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (9, 9, 25, 'REQ-012', 'Low priority setup', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (10, 10, 50, 'REQ-013', 'Search functionality ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (11, 11, 35, 'REQ-014', 'Search system initialized', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (12, 12, 40, 'REQ-015', 'Login system ready', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (13, 13, 30, 'REQ-016', 'API endpoints available', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (14, 14, 45, 'REQ-017', 'Version control ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (15, 15, 60, 'REQ-018', 'Complex scenario setup', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (16, 16, 35, 'REQ-019', 'Draft version setup', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (17, 17, 20, 'REQ-020', 'Full-text search ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (18, 18, 25, 'REQ-021', 'Search indexing complete', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (19, 19, 30, 'REQ-022', 'Search filters ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (20, 20, 15, 'REQ-023', 'Batch processing ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (21, 21, 20, 'REQ-024', 'Priority batch ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (22, 22, 25, 'REQ-025', 'Tag management ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (23, 23, 30, 'REQ-026', 'Tag processing ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (24, 24, 20, 'REQ-027', 'Null field handling ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (25, 25, 25, 'REQ-028', 'Null validation ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (26, 26, 35, 'REQ-029', 'Attribute validation ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (27, 27, 40, 'REQ-030', 'Invalid attribute handling', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (28, 28, 45, 'REQ-031', 'Full attribute processing', 'STEPS');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (29, 29, 30, 'REQ-032', 'Complete attribute setup', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (30, 30, 25, 'REQ-033', 'Folder validation ready', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (31, 31, 30, 'REQ-034', 'Non-existing folder handling', 'TEXT');

insert into tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, preconditions, type)
values (32, 32, 35, 'REQ-035', 'Final version processing', 'STEPS');

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

-- Manual scenario attributes
insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (1, 7, 'high');

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (2, 8, 'production');

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (10, 7, 'critical');

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (12, 8, 'staging');

insert into tms_manual_scenario_attribute (manual_scenario_id, attribute_id, value)
values (15, 7, 'medium');

-- Steps for STEPS scenarios
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

-- Set sequences to continue from highest ID
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_step_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_step));
