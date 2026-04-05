-- ==================== TEST FOLDERS ====================

-- Root folders for different test scenarios
INSERT INTO tms_test_folder (id, project_id, description, "name", "index")
VALUES (3, 1, 'description_folder3', 'name_folder3', 0),
       (4, 1, 'description_folder4', 'name_folder4', 1),
       (5, 1, 'description_folder5', 'name_folder5', 2);

-- Subfolders of folder 3 (used for hierarchy duplication tests)
INSERT INTO tms_test_folder (id, project_id, description, "name", "index", parent_id)
VALUES (6, 1, 'description_subfolder1', 'name_subfolder1', 0, 3),
       (7, 1, 'description_subfolder2', 'name_subfolder2', 1, 3);

-- Sub-subfolder of folder 6 (nested hierarchy test)
INSERT INTO tms_test_folder (id, project_id, description, "name", "index", parent_id)
VALUES (8, 1, 'description_subsubfolder', 'name_subsubfolder', 0, 6);

-- Subfolder of folder 4
INSERT INTO tms_test_folder (id, project_id, description, "name", "index", parent_id)
VALUES (9, 1, 'description_subfolder_of_4', 'name_subfolder_of_4', 0, 4);

-- Additional subfolders for export testing
INSERT INTO tms_test_folder (id, project_id, description, "name", "index", parent_id)
VALUES (10, 1, 'This is a longer description with special characters: !@#$%^&*()',
        'Export Test Folder', 2, 3),
       (11, 1, NULL, 'Empty Description Folder', 3, 3);

-- Root folder for export testing with complex hierarchy
INSERT INTO tms_test_folder (id, project_id, description, "name", "index")
VALUES (12, 1, 'Root folder for export testing', 'Export Root', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name", "index", parent_id)
VALUES (13, 1, 'First level subfolder 1',   'Export Sub 1',       0, 12),
       (14, 1, 'First level subfolder 2',   'Export Sub 2',       1, 12),
       (15, 1, 'Second level subfolder 1',  'Export Sub-Sub 1',   0, 13),
       (16, 1, 'Second level subfolder 2',  'Export Sub-Sub 2',   1, 13),
       (17, 1, 'Second level subfolder 3',  'Export Sub-Sub 3',   0, 14),
       (18, 1, 'Third level subfolder',     'Export Sub-Sub-Sub', 0, 15);

-- Test folders for test plan integration tests
INSERT INTO tms_test_folder (id, "name", description, project_id, "index", parent_id)
VALUES (19, 'Root Test Folder',  'Root test folder description',    1, 4, NULL),
       (20, 'Sub Test Folder 1', 'Sub test folder 1 description',   1, 0, 19),
       (21, 'Sub Test Folder 2', 'Sub test folder 2 description',   1, 1, 19);

INSERT INTO tms_test_folder (id, project_id, description, "name", "index", parent_id)
VALUES (22, 1, 'new_description_subfolder_of_3', 'new_name_subfolder_of_4', 4, 3);

-- ==================== TEST ATTRIBUTES ====================

INSERT INTO tms_attribute (id, "key", value, project_id)
VALUES (10, 'test_key_10', NULL, 1),
       (11, 'test_key_11', NULL, 1),
       (12, 'test_key_12', NULL, 1);

INSERT INTO tms_attribute (id, "key", value, project_id)
VALUES (13, 'priority', 'high',   1),
       (14, 'priority', 'medium', 1),
       (15, 'priority', 'low',    1);

INSERT INTO tms_attribute (id, "key", value, project_id)
VALUES (16, 'component', 'core',         1),
       (17, 'component', 'api',          1),
       (18, 'component', 'ui',           1),
       (19, 'component', 'integration',  1),
       (20, 'component', 'smoke',        1),
       (21, 'component', 'regression',   1),
       (22, 'component', 'performance',  1),
       (23, 'component', 'functional',   1);

INSERT INTO tms_attribute (id, "key", value, project_id)
VALUES (24, 'environment', 'staging',    1),
       (25, 'environment', 'production', 1),
       (26, 'environment', 'dev',        1),
       (27, 'environment', 'qa',         1);

-- ==================== TEST PLANS ====================
-- All test plans belong to project 1: TP-1, TP-2, TP-3
INSERT INTO tms_test_plan (id, "name", description, project_id, display_id, environment_id, product_version_id)
VALUES (4, 'Test Plan 4', 'Test plan 4 description', 1, 'TP-1', null, null),
       (5, 'Test Plan 5', 'Test plan 5 description', 1, 'TP-2', null, null),
       (6, 'Test Plan 6', 'Test plan 6 description', 1, 'TP-3', null, null);

-- ==================== TEST CASES FOR DUPLICATION TESTS ====================
-- All folders belong to project 1: TC-1..TC-19

-- Test cases in root folder 3
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (100, 'Test Case in Folder 3 - TC100', 'First test case in root folder 3',  3, 'HIGH',   1, 'TC-1'),
       (101, 'Test Case in Folder 3 - TC101', 'Second test case in root folder 3', 3, 'MEDIUM', 1, 'TC-2'),
       (102, 'Test Case in Folder 3 - TC102', 'Third test case in root folder 3',  3, 'LOW',    1, 'TC-3');

-- Test cases in subfolder 6 (child of folder 3)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (103, 'Test Case in Subfolder 6 - TC103', 'Test case in first subfolder',         6, 'HIGH',   1, 'TC-4'),
       (104, 'Test Case in Subfolder 6 - TC104', 'Another test case in first subfolder', 6, 'MEDIUM', 1, 'TC-5');

-- Test cases in subfolder 7 (child of folder 3)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (105, 'Test Case in Subfolder 7 - TC105', 'Test case in second subfolder',         7, 'LOW',  1, 'TC-6'),
       (106, 'Test Case in Subfolder 7 - TC106', 'Another test case in second subfolder', 7, 'HIGH', 1, 'TC-7');

-- Test cases in sub-subfolder 8 (child of folder 6, grandchild of folder 3)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (107, 'Test Case in SubSubfolder 8 - TC107', 'Test case in nested subfolder',         8, 'MEDIUM', 1, 'TC-8'),
       (108, 'Test Case in SubSubfolder 8 - TC108', 'Another test case in nested subfolder', 8, 'HIGH',   1, 'TC-9');

-- Test cases in folder 4
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (109, 'Test Case in Folder 4 - TC109', 'Test case in folder 4',         4, 'HIGH', 1, 'TC-10'),
       (110, 'Test Case in Folder 4 - TC110', 'Another test case in folder 4', 4, 'LOW',  1, 'TC-11');

-- Test cases in subfolder 9 (child of folder 4)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (111, 'Test Case in Subfolder 9 - TC111', 'Test case in subfolder of 4', 9, 'MEDIUM', 1, 'TC-12');

-- Test cases in folder 5
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (112, 'Test Case in Folder 5 - TC112', 'Test case in folder without subfolders', 5, 'HIGH',   1, 'TC-13'),
       (113, 'Test Case in Folder 5 - TC113', 'Another test case in folder 5',          5, 'MEDIUM', 1, 'TC-14'),
       (114, 'Test Case in Folder 5 - TC114', 'Third test case in folder 5',            5, 'LOW',    1, 'TC-15');

-- Test cases in test plan folders (19, 20, 21)
INSERT INTO tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
VALUES (115, 'Test Case in Folder 19 - TC115', 'Test case in root test folder',             19, 'HIGH',   1, 'TC-16'),
       (116, 'Test Case in Folder 20 - TC116', 'Test case in sub test folder 1',            20, 'MEDIUM', 1, 'TC-17'),
       (117, 'Test Case in Folder 20 - TC117', 'Another test case in sub test folder 1',    20, 'HIGH',   1, 'TC-18'),
       (118, 'Test Case in Folder 21 - TC118', 'Test case in sub test folder 2',            21, 'LOW',    1, 'TC-19');

-- ==================== TEST CASE VERSIONS ====================

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

INSERT INTO tms_manual_scenario (id, test_case_version_id, execution_estimation_time, type)
VALUES (100, 100, 30, 'TEXT'),
       (101, 101, 25, 'TEXT'),
       (102, 102, 20, 'TEXT'),
       (103, 103, 35, 'TEXT'),
       (104, 104, 40, 'TEXT'),
       (105, 105, 15, 'TEXT'),
       (106, 106, 45, 'TEXT'),
       (107, 107, 30, 'TEXT'),
       (108, 108, 35, 'TEXT'),
       (109, 109, 50, 'TEXT'),
       (110, 110, 20, 'TEXT'),
       (111, 111, 30, 'TEXT'),
       (112, 112, 25, 'TEXT'),
       (113, 113, 30, 'TEXT'),
       (114, 114, 20, 'TEXT'),
       (115, 115, 40, 'TEXT'),
       (116, 116, 35, 'TEXT'),
       (117, 117, 45, 'TEXT'),
       (118, 118, 25, 'TEXT');

-- Manual scenario requirements
INSERT INTO tms_manual_scenario_requirement (id, value, manual_scenario_id, number)
VALUES ('REQ-100', 'Requirement for TC100', 100, 0),
       ('REQ-101', 'Requirement for TC101', 101, 0),
       ('REQ-102', 'Requirement for TC102', 102, 0),
       ('REQ-103', 'Requirement for TC103', 103, 0),
       ('REQ-104', 'Requirement for TC104', 104, 0),
       ('REQ-105', 'Requirement for TC105', 105, 0),
       ('REQ-106', 'Requirement for TC106', 106, 0),
       ('REQ-107', 'Requirement for TC107', 107, 0),
       ('REQ-108', 'Requirement for TC108', 108, 0),
       ('REQ-109', 'Requirement for TC109', 109, 0),
       ('REQ-110', 'Requirement for TC110', 110, 0),
       ('REQ-111', 'Requirement for TC111', 111, 0),
       ('REQ-112', 'Requirement for TC112', 112, 0),
       ('REQ-113', 'Requirement for TC113', 113, 0),
       ('REQ-114', 'Requirement for TC114', 114, 0),
       ('REQ-115', 'Requirement for TC115', 115, 0),
       ('REQ-116', 'Requirement for TC116', 116, 0),
       ('REQ-117', 'Requirement for TC117', 117, 0),
       ('REQ-118', 'Requirement for TC118', 118, 0);

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
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (100, 13),
       (100, 16),
       (101, 14),
       (101, 24),
       (102, 15),
       (103, 13),
       (103, 17),
       (104, 14),
       (105, 15),
       (105, 25),
       (106, 13),
       (106, 18),
       (107, 14),
       (107, 19),
       (108, 13),
       (108, 20);

-- Attributes for test cases in folder 4 hierarchy
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (109, 13),
       (109, 21),
       (110, 15),
       (111, 14),
       (111, 26);

-- Attributes for test cases in folder 5
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (112, 13),
       (112, 22),
       (113, 14),
       (114, 15),
       (114, 27);

-- Attributes for test cases in test plan folders
INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES (115, 13),
       (116, 14),
       (116, 23),
       (117, 13),
       (117, 19),
       (118, 15);

-- ==================== TEST PLAN TEST CASE LINKS ====================

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

-- ==================== PROJECT SEQUENCES ====================

INSERT INTO tms_project_sequence (project_id, entity_type, current_value)
VALUES (1, 'TEST_CASE', 19),
       (1, 'TEST_PLAN', 3);

-- ==================== UPDATE SEQUENCES ====================

SELECT setval('tms_test_folder_id_seq',                   (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq',                     (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_case_version_id_seq',             (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq',               (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_manual_scenario_preconditions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario_preconditions));
SELECT setval('tms_attribute_id_seq',                     (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
SELECT setval('tms_test_plan_id_seq',                     (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
