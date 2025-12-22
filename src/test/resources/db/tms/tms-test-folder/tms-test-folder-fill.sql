ALTER SEQUENCE IF EXISTS tms_test_folder_id_seq RESTART WITH 1;

-- Root folders for different test scenarios
INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (3, 1, 'description_folder3', 'name_folder3');

INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (4, 1, 'description_folder4', 'name_folder4');

INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (5, 1, 'description_folder5', 'name_folder5');

-- Subfolders of folder 3 (used for hierarchy duplication tests)
INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (6, 1, 'description_subfolder1', 'name_subfolder1', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (7, 1, 'description_subfolder2', 'name_subfolder2', 3);

-- Sub-subfolder of folder 6 (nested hierarchy test)
INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (8, 1, 'description_subsubfolder', 'name_subsubfolder', 6);

-- Subfolder of folder 4
INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (9, 1, 'description_subfolder_of_4', 'name_subfolder_of_4', 4);

-- Additional subfolders for export testing
INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (10, 1, 'This is a longer description with special characters: !@#$%^&*()',
        'Export Test Folder', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (11, 1, NULL, 'Empty Description Folder', 3);

-- Root folder for export testing with complex hierarchy
INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (12, 1, 'Root folder for export testing', 'Export Root');

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (13, 1, 'First level subfolder 1', 'Export Sub 1', 12);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (14, 1, 'First level subfolder 2', 'Export Sub 2', 12);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (15, 1, 'Second level subfolder 1', 'Export Sub-Sub 1', 13);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (16, 1, 'Second level subfolder 2', 'Export Sub-Sub 2', 13);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (17, 1, 'Second level subfolder 3', 'Export Sub-Sub 3', 14);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (18, 1, 'Third level subfolder', 'Export Sub-Sub-Sub', 15);

-- Test folders for test plan integration tests
INSERT INTO tms_test_folder (id, "name", description, project_id, parent_id)
VALUES (19, 'Root Test Folder', 'Root test folder description', 1, NULL),
       (20, 'Sub Test Folder 1', 'Sub test folder 1 description', 1, 19),
       (21, 'Sub Test Folder 2', 'Sub test folder 2 description', 1, 19);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (22, 1, 'new_description_subfolder_of_3', 'new_name_subfolder_of_4', 3);

-- ==================== TEST ATTRIBUTES ====================
INSERT INTO tms_attribute (id, "key", project_id)
VALUES (10, 'test_key_10', 1),
       (11, 'test_key_11', 1),
       (12, 'test_key_12', 1),
       (13, 'priority', 1),
       (14, 'component', 1),
       (15, 'environment', 1);

-- ==================== TEST PLANS ====================
INSERT INTO tms_test_plan (id, "name", description, project_id, environment_id, product_version_id)
VALUES (4, 'Test Plan 4', 'Test plan 4 description', 1, null, null),
       (5, 'Test Plan 5', 'Test plan 5 description', 1, null, null),
       (6, 'Test Plan 6', 'Test plan 6 description', 1, null, null);

-- ==================== TEST CASES FOR DUPLICATION TESTS ====================

-- Test cases in root folder 3 (used for duplication testing)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (100, 'Test Case in Folder 3 - TC100', 'First test case in root folder 3', 3, 'HIGH'),
       (101, 'Test Case in Folder 3 - TC101', 'Second test case in root folder 3', 3, 'MEDIUM'),
       (102, 'Test Case in Folder 3 - TC102', 'Third test case in root folder 3', 3, 'LOW');

-- Test cases in subfolder 6 (child of folder 3)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (103, 'Test Case in Subfolder 6 - TC103', 'Test case in first subfolder', 6, 'HIGH'),
       (104, 'Test Case in Subfolder 6 - TC104', 'Another test case in first subfolder', 6, 'MEDIUM');

-- Test cases in subfolder 7 (child of folder 3)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (105, 'Test Case in Subfolder 7 - TC105', 'Test case in second subfolder', 7, 'LOW'),
       (106, 'Test Case in Subfolder 7 - TC106', 'Another test case in second subfolder', 7, 'HIGH');

-- Test cases in sub-subfolder 8 (child of folder 6, grandchild of folder 3)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (107, 'Test Case in SubSubfolder 8 - TC107', 'Test case in nested subfolder', 8, 'MEDIUM'),
       (108, 'Test Case in SubSubfolder 8 - TC108', 'Another test case in nested subfolder', 8, 'HIGH');

-- Test cases in folder 4 (used for simple duplication testing)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (109, 'Test Case in Folder 4 - TC109', 'Test case in folder 4', 4, 'HIGH'),
       (110, 'Test Case in Folder 4 - TC110', 'Another test case in folder 4', 4, 'LOW');

-- Test cases in subfolder 9 (child of folder 4)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (111, 'Test Case in Subfolder 9 - TC111', 'Test case in subfolder of 4', 9, 'MEDIUM');

-- Test cases in folder 5 (empty hierarchy - no subfolders but has test cases)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (112, 'Test Case in Folder 5 - TC112', 'Test case in folder without subfolders', 5, 'HIGH'),
       (113, 'Test Case in Folder 5 - TC113', 'Another test case in folder 5', 5, 'MEDIUM'),
       (114, 'Test Case in Folder 5 - TC114', 'Third test case in folder 5', 5, 'LOW');

-- Test cases in test plan folders (19, 20, 21)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority)
VALUES (115, 'Test Case in Folder 19 - TC115', 'Test case in root test folder', 19, 'HIGH'),
       (116, 'Test Case in Folder 20 - TC116', 'Test case in sub test folder 1', 20, 'MEDIUM'),
       (117, 'Test Case in Folder 20 - TC117', 'Another test case in sub test folder 1', 20, 'HIGH'),
       (118, 'Test Case in Folder 21 - TC118', 'Test case in sub test folder 2', 21, 'LOW');

-- ==================== TEST CASE VERSIONS ====================

-- Default versions for all test cases
INSERT INTO tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
VALUES (100, 100, 'Default Version', true, false),
       (101, 101, 'Default Version', true, false),
       (102, 102, 'Default Version', true, false),
       (103, 103, 'Default Version', true, false),
       (104, 104, 'Default Version', true, false),
       (105, 105, 'Default Version', true, false),
       (106, 106, 'Default Version', true, false),
       (107, 107, 'Default Version', true, false),
       (108, 108, 'Default Version', true, false),
       (109, 109, 'Default Version', true, false),
       (110, 110, 'Default Version', true, false),
       (111, 111, 'Default Version', true, false),
       (112, 112, 'Default Version', true, false),
       (113, 113, 'Default Version', true, false),
       (114, 114, 'Default Version', true, false),
       (115, 115, 'Default Version', true, false),
       (116, 116, 'Default Version', true, false),
       (117, 117, 'Default Version', true, false),
       (118, 118, 'Default Version', true, false);

-- ==================== MANUAL SCENARIOS ====================

-- TEXT manual scenarios for test cases
INSERT INTO tms_manual_scenario (id, test_case_version_id, execution_estimation_time, link_to_requirements, type)
VALUES (100, 100, 30, 'REQ-100', 'TEXT'),
       (101, 101, 25, 'REQ-101', 'TEXT'),
       (102, 102, 20, 'REQ-102', 'TEXT'),
       (103, 103, 35, 'REQ-103', 'TEXT'),
       (104, 104, 40, 'REQ-104', 'TEXT'),
       (105, 105, 15, 'REQ-105', 'TEXT'),
       (106, 106, 45, 'REQ-106', 'TEXT'),
       (107, 107, 30, 'REQ-107', 'TEXT'),
       (108, 108, 35, 'REQ-108', 'TEXT'),
       (109, 109, 50, 'REQ-109', 'TEXT'),
       (110, 110, 20, 'REQ-110', 'TEXT'),
       (111, 111, 30, 'REQ-111', 'TEXT'),
       (112, 112, 25, 'REQ-112', 'TEXT'),
       (113, 113, 30, 'REQ-113', 'TEXT'),
       (114, 114, 20, 'REQ-114', 'TEXT'),
       (115, 115, 40, 'REQ-115', 'TEXT'),
       (116, 116, 35, 'REQ-116', 'TEXT'),
       (117, 117, 45, 'REQ-117', 'TEXT'),
       (118, 118, 25, 'REQ-118', 'TEXT');

-- Manual scenario preconditions
INSERT INTO tms_manual_scenario_preconditions (id, manual_scenario_id, value)
VALUES (100, 100, 'System must be initialized for TC100'),
       (101, 101, 'User must be authenticated for TC101'),
       (102, 102, 'Test environment ready for TC102'),
       (103, 103, 'Database initialized for TC103'),
       (104, 104, 'Network connectivity for TC104'),
       (105, 105, 'Test data prepared for TC105'),
       (106, 106, 'System configuration for TC106'),
       (107, 107, 'Application deployed for TC107'),
       (108, 108, 'API endpoints available for TC108'),
       (109, 109, 'Prerequisites met for TC109'),
       (110, 110, 'Setup complete for TC110'),
       (111, 111, 'Environment ready for TC111'),
       (112, 112, 'System ready for TC112'),
       (113, 113, 'Prerequisites for TC113'),
       (114, 114, 'Setup for TC114'),
       (115, 115, 'Ready for TC115'),
       (116, 116, 'Setup for TC116'),
       (117, 117, 'Ready for TC117'),
       (118, 118, 'Setup for TC118');

-- Text manual scenarios details
INSERT INTO tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
VALUES (100, 'Execute test case 100 functionality', 'Test should pass with high priority'),
       (101, 'Execute test case 101 functionality', 'Test should pass with medium priority'),
       (102, 'Execute test case 102 functionality', 'Test should pass with low priority'),
       (103, 'Execute test case 103 functionality', 'Subfolder test should pass'),
       (104, 'Execute test case 104 functionality', 'Subfolder test should complete'),
       (105, 'Execute test case 105 functionality', 'Second subfolder test should pass'),
       (106, 'Execute test case 106 functionality', 'High priority subfolder test'),
       (107, 'Execute test case 107 functionality', 'Nested subfolder test should pass'),
       (108, 'Execute test case 108 functionality', 'Nested test should complete'),
       (109, 'Execute test case 109 functionality', 'Folder 4 test should pass'),
       (110, 'Execute test case 110 functionality', 'Another folder 4 test'),
       (111, 'Execute test case 111 functionality', 'Subfolder 9 test should pass'),
       (112, 'Execute test case 112 functionality', 'Folder 5 test should pass'),
       (113, 'Execute test case 113 functionality', 'Another folder 5 test'),
       (114, 'Execute test case 114 functionality', 'Third folder 5 test'),
       (115, 'Execute test case 115 functionality', 'Root test folder test'),
       (116, 'Execute test case 116 functionality', 'Sub test folder 1 test'),
       (117, 'Execute test case 117 functionality', 'Another sub test folder 1 test'),
       (118, 'Execute test case 118 functionality', 'Sub test folder 2 test');

-- ==================== TEST CASE ATTRIBUTES ====================

-- Attributes for test cases in folder 3 hierarchy
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id, value)
VALUES (100, 13, 'high'),
       (100, 14, 'core'),
       (101, 13, 'medium'),
       (101, 15, 'staging'),
       (102, 13, 'low'),
       (103, 13, 'high'),
       (103, 14, 'api'),
       (104, 13, 'medium'),
       (105, 13, 'low'),
       (105, 15, 'production'),
       (106, 13, 'high'),
       (106, 14, 'ui'),
       (107, 13, 'medium'),
       (107, 14, 'integration'),
       (108, 13, 'high'),
       (108, 14, 'smoke');

-- Attributes for test cases in folder 4 hierarchy
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id, value)
VALUES (109, 13, 'high'),
       (109, 14, 'regression'),
       (110, 13, 'low'),
       (111, 13, 'medium'),
       (111, 15, 'dev');

-- Attributes for test cases in folder 5
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id, value)
VALUES (112, 13, 'high'),
       (112, 14, 'performance'),
       (113, 13, 'medium'),
       (114, 13, 'low'),
       (114, 15, 'qa');

-- Attributes for test cases in test plan folders
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id, value)
VALUES (115, 13, 'high'),
       (116, 13, 'medium'),
       (116, 14, 'functional'),
       (117, 13, 'high'),
       (117, 14, 'integration'),
       (118, 13, 'low');

-- ==================== TEST PLAN TEST CASE LINKS ====================

-- Link test cases to test plans
INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id)
VALUES (4, 100),
       (4, 101),
       (4, 103),
       (5, 102),
       (5, 104),
       (5, 105),
       (6, 106),
       (6, 107),
       (6, 108),
       (4, 109),
       (5, 110),
       (6, 111),
       (4, 112),
       (5, 113),
       (6, 114),
       (4, 115),
       (4, 116),
       (5, 117),
       (6, 118);

-- ==================== UPDATE SEQUENCES ====================

SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_manual_scenario_preconditions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario_preconditions));
SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
