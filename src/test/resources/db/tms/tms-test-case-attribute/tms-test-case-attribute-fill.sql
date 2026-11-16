-- Test folders
INSERT INTO tms_test_folder (id, "name", description, project_id)
VALUES (3, 'Test Folder 3', 'Description for test folder 3', 1),
       (4, 'Test Folder 4', 'Description for test folder 4', 1),
       (5, 'Test Folder 5', 'Description for test folder 5', 2);

-- Attributes for project 1
INSERT INTO tms_attribute (id, "key", value, project_id)
VALUES (1, 'browser', 'chrome', 1),
       (2, 'browser', 'firefox', 1),
       (3, 'priority', 'high', 1),
       (4, 'priority', 'low', 1),
       (5, 'environment', 'staging', 1),
       (6, 'os', 'linux', 1);

-- Attributes for project 2 (should NOT appear in project 1 results)
INSERT INTO tms_attribute (id, "key", value, project_id)
VALUES (7, 'browser', 'safari', 2),
       (8, 'priority', 'medium', 2);

-- Test cases in project 1 (folders 3 and 4)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (1, 'Test Case 1', 'Description for test case 1', 3, 'HIGH', 1, 'TC-1'),
       (2, 'Test Case 2', 'Description for test case 2', 3, 'MEDIUM', 1, 'TC-2'),
       (3, 'Test Case 3', 'Description for test case 3', 4, 'LOW', 1, 'TC-3'),
       (4, 'Test Case 4 - No Attributes', 'Test case without any attributes', 4, 'LOW', 1, 'TC-4'),
       (5, 'Test Case 5', 'Description for test case 5', 3, 'HIGH', 1, 'TC-5');

-- Test cases in project 2 (folder 5)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (6, 'Test Case 6 - Project 2', 'Test case in project 2', 5, 'HIGH', 2, 'TC-1');

-- Test case attributes for project 1 test cases
-- Test case 1: browser=chrome, priority=high
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (1, 1),
       (1, 3);

-- Test case 2: browser=chrome, browser=firefox, environment=staging
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (2, 1),
       (2, 2),
       (2, 5);

-- Test case 3: priority=low, os=linux
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (3, 4),
       (3, 6);

-- Test case 4: no attributes (used for empty result testing)

-- Test case 5: browser=chrome, priority=high, environment=staging (shares attributes with TC1 and TC2)
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (5, 1),
       (5, 3),
       (5, 5);

-- Test case 6 (project 2): browser=safari, priority=medium
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (6, 7),
       (6, 8);

-- Project sequences for display_id tracking
INSERT INTO tms_project_sequence (project_id, entity_type, current_value)
VALUES (1, 'TEST_CASE', 5),
       (2, 'TEST_CASE', 1);

-- Update sequences
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
