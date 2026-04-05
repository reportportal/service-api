-- =====================================================
-- STATISTICS FIELDS
-- =====================================================

INSERT INTO statistics_field (name)
VALUES
    ('statistics$executions$total'),
    ('statistics$executions$passed'),
    ('statistics$executions$failed'),
    ('statistics$executions$skipped'),
    ('statistics$defects$product_bug$total'),
    ('statistics$defects$automation_bug$total'),
    ('statistics$defects$system_issue$total'),
    ('statistics$defects$to_investigate$total')
    ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- TMS TEST FOLDERS
-- =====================================================

INSERT INTO tms_test_folder (id, name, description, parent_id, project_id)
VALUES
    (1, 'Smoke Tests', 'Critical smoke test scenarios', NULL, 1),
    (2, 'Regression Tests', 'Full regression test suite', NULL, 1),
    (3, 'Integration Tests', 'API and integration tests', NULL, 1),
    (4, 'Smoke Tests - Login', 'Login smoke tests subfolder', 1, 1),
    (5, 'Default Project Folder', 'Test folder for default project', NULL, 2),
    (6, 'Test Folder 6', 'Description for test folder 6', NULL, 1),
    (7, 'Search Folder', 'Folder for search tests', NULL, 1),
    (8, 'Search Folder 2', 'Folder for full-text search tests', NULL, 1),
    (9, 'Test Folder 9', 'Description for test folder 9', NULL, 2),
    (10, 'Test Folder 10', 'Description for test folder 10', NULL, 1),
    (11, 'Test Folder 11', 'Description for test folder 11', NULL, 2);

-- =====================================================
-- TMS TEST PLANS
-- project 1: ids 1,2,4,5,6 → TP-1..TP-5
-- project 2: id 3          → TP-1
-- =====================================================

INSERT INTO tms_test_plan (id, name, description, project_id, display_id, created_at, updated_at)
VALUES
    (1, 'Sprint 25 Test Plan',      'Test plan for Sprint 25 release',          1, 'TP-1', NOW(), NOW()),
    (2, 'Hotfix Test Plan',         'Quick test plan for hotfix verification',   1, 'TP-2', NOW(), NOW()),
    (3, 'Default Project Test Plan','Test plan for default project',             2, 'TP-1', NOW(), NOW()),
    (4, 'Integration Test Plan',    'Test plan for integration tests',           1, 'TP-3', NOW(), NOW()),
    (5, 'Regression Test Plan',     'Test plan for regression tests',            1, 'TP-4', NOW(), NOW()),
    (6, 'Empty Test Plan',          'Empty Test plan for regression tests',      1, 'TP-5', NOW(), NOW());

-- =====================================================
-- TMS TEST CASES
-- project_id берётся из соответствующей папки:
--   folders 1,2,3,4,6,7,8,10 → project_id=1
--   folders 5,9,11            → project_id=2
--
-- display_id нумеруется независимо по каждому проекту:
--   project 1: TC-1..TC-32
--   project 2: TC-1..TC-6
-- =====================================================

INSERT INTO tms_test_case (id, name, description, priority, test_folder_id, project_id, display_id, created_at, updated_at)
VALUES
    -- project 1, folder 1
    (4,  'User Login with Valid Credentials',    'Verify user can login with valid username and password',    'CRITICAL', 1,  1, 'TC-1',  NOW(), NOW()),
    (5,  'User Login with Invalid Credentials',  'Verify appropriate error message for invalid credentials',  'HIGH',     1,  1, 'TC-2',  NOW(), NOW()),
    (6,  'Password Reset Flow',                  'Verify complete password reset functionality',              'MEDIUM',   1,  1, 'TC-3',  NOW(), NOW()),
    -- project 1, folder 2
    (7,  'User Profile Update',                  'Verify user can update profile information',                'MEDIUM',   2,  1, 'TC-4',  NOW(), NOW()),
    (8,  'Data Export Functionality',            'Verify data can be exported in CSV format',                 'LOW',      2,  1, 'TC-5',  NOW(), NOW()),
    -- project 1, folder 3
    (9,  'API Integration Test',                 'Verify REST API endpoints respond correctly',               'HIGH',     3,  1, 'TC-6',  NOW(), NOW()),
    (10, 'Database Connection Test',             'Verify database connectivity and queries',                  'CRITICAL', 3,  1, 'TC-7',  NOW(), NOW()),
    -- project 2, folder 5
    (11, 'Default Project Test Case',            'Test case in default project',                              'MEDIUM',   5,  2, 'TC-1',  NOW(), NOW()),
    (12, 'Test Case 12',                         'Description for test case 12',                              'LOW',      5,  2, 'TC-2',  NOW(), NOW()),
    -- project 1, folder 7
    (13, 'Search Test Case',                     'This is a searchable test case',                            'HIGH',     7,  1, 'TC-8',  NOW(), NOW()),
    (14, 'Another Test',                         'Another description for search',                            'MEDIUM',   7,  1, 'TC-9',  NOW(), NOW()),
    (15, 'Login Test Case',                      'Test case for login functionality',                         'HIGH',     7,  1, 'TC-10', NOW(), NOW()),
    (16, 'API Test Case',                        'Test case for API testing',                                 'MEDIUM',   7,  1, 'TC-11', NOW(), NOW()),
    -- project 1, folder 6
    (17, 'Test Case With Version',               'Test case with default version',                            'HIGH',     6,  1, 'TC-12', NOW(), NOW()),
    (18, 'Complex Test Case',                    'Test case with complex scenario',                           'MEDIUM',   6,  1, 'TC-13', NOW(), NOW()),
    -- project 1, folder 8
    (19, '3test',                                'Test for full-text search 1',                               'LOW',      8,  1, 'TC-14', NOW(), NOW()),
    (20, 'Test for full-text search 2',          'Test for full-text search 2',                               'MEDIUM',   8,  1, 'TC-15', NOW(), NOW()),
    -- project 1, folder 7
    (21, '3test',                                'Test for full-text search 3',                               'MEDIUM',   7,  1, 'TC-16', NOW(), NOW()),
    -- project 1, folder 4
    (22, 'Batch Test Case 22',                   'Test case for batch patch with priority',                   'LOW',      4,  1, 'TC-17', NOW(), NOW()),
    (23, 'Batch Test Case 23',                   'Test case for batch patch with priority',                   'LOW',      4,  1, 'TC-18', NOW(), NOW()),
    -- project 2, folder 5
    (24, 'Batch Test Case 24',                   'Test case for batch patch with tags only',                  'MEDIUM',   5,  2, 'TC-3',  NOW(), NOW()),
    (25, 'Batch Test Case 25',                   'Test case for batch patch with tags only',                  'MEDIUM',   5,  2, 'TC-4',  NOW(), NOW()),
    -- project 1, folder 6
    (26, 'Batch Test Case 26',                   'Test case for batch patch with null fields',                'HIGH',     6,  1, 'TC-19', NOW(), NOW()),
    (27, 'Batch Test Case 27',                   'Test case for batch patch with null fields',                'HIGH',     6,  1, 'TC-20', NOW(), NOW()),
    -- project 1, folder 3
    (28, 'Batch Test Case 28',                   'Test case for batch patch with invalid attribute',          'MEDIUM',   3,  1, 'TC-21', NOW(), NOW()),
    (29, 'Batch Test Case 29',                   'Test case for batch patch with invalid attribute',          'MEDIUM',   3,  1, 'TC-22', NOW(), NOW()),
    (30, 'Batch Test Case 30',                   'Test case for batch patch with full attributes',            'MEDIUM',   3,  1, 'TC-23', NOW(), NOW()),
    (31, 'Batch Test Case 31',                   'Test case for batch patch with full attributes',            'MEDIUM',   3,  1, 'TC-24', NOW(), NOW()),
    (32, 'Batch Test Case 32',                   'Test case for batch patch with non existing folder 32',     'MEDIUM',   3,  1, 'TC-25', NOW(), NOW()),
    (33, 'Batch Test Case 33',                   'Test case for batch patch with non existing folder 33',     'MEDIUM',   3,  1, 'TC-26', NOW(), NOW()),
    -- project 1, folder 6
    (34, 'Test Case With Version 34',            'Test case with default version 34',                         'HIGH',     6,  1, 'TC-27', NOW(), NOW()),
    -- project 2, folder 9
    (35, 'Test Case With Version 35',            'Test case with default version 35',                         'HIGH',     9,  2, 'TC-5',  NOW(), NOW()),
    (36, 'Test Case With Version 36',            'Test case with default version 36',                         'HIGH',     9,  2, 'TC-6',  NOW(), NOW()),
    -- project 1, folder 10
    (37, 'Test Case With Version 37',            'Test case with default version 37',                         'HIGH',     10, 1, 'TC-28', NOW(), NOW()),
    -- project 1, folder 3
    (100,'Test Case with Last Execution',        'Test case that has execution data',                         'HIGH',     3,  1, 'TC-29', NOW(), NOW()),
    (101,'Test Case with Multiple Executions',   'Test case with multiple executions, should return latest',  'MEDIUM',   3,  1, 'TC-30', NOW(), NOW()),
    (102,'Test Case without Execution',          'Test case with no execution data',                          'LOW',      3,  1, 'TC-31', NOW(), NOW()),
    (103,'Test Case with Single Execution',      'Test case with single execution',                           'HIGH',     3,  1, 'TC-32', NOW(), NOW());

-- =====================================================
-- TMS TEST PLAN TEST CASES
-- =====================================================

INSERT INTO tms_test_plan_test_case (test_plan_id, test_case_id)
VALUES
    (1, 4), (1, 5), (1, 6),
    (2, 7), (2, 8), (2, 9),
    (3, 10), (3, 11), (3, 12),
    (4, 13), (4, 14), (4, 15),
    (5, 16), (5, 17), (5, 18),
    (4, 4), (4, 5),
    (1, 13), (2, 15),
    (1, 14), (1, 15), (1, 16),
    (2, 19), (3, 20);

-- =====================================================
-- TMS ATTRIBUTES
-- =====================================================

INSERT INTO tms_attribute (id, key, value, project_id)
VALUES
    (1, 'test1', NULL, 1),
    (2, 'test2', NULL, 1),
    (3, 'test3', NULL, 1),
    (4, 'test4', NULL, 1),
    (5, 'test5', NULL, 1),
    (6, 'test6', NULL, 1),
    (7, 'priority', NULL, 1),
    (8, 'environment', NULL, 1);

INSERT INTO tms_attribute (id, key, value, project_id)
VALUES
    (9,  'test1',       'existing-tag-22',  1),
    (10, 'test1',       'existing-tag-25',  1),
    (11, 'test1',       'existing-tag-36',  1),
    (12, 'test1',       'existing-tag-37-1',1),
    (13, 'test2',       'existing-tag-23',  1),
    (14, 'test2',       'existing-tag-37-2',1),
    (15, 'test3',       'existing-tag-24',  1),
    (16, 'test4',       'test value 4',     1),
    (17, 'test4',       'test value 7',     1),
    (18, 'test4',       'test value 9',     1),
    (19, 'test5',       'test value 5',     1),
    (20, 'test5',       'test value 8',     1),
    (21, 'test5',       'test value 10',    1),
    (22, 'priority',    'high',             1),
    (23, 'priority',    'critical',         1),
    (24, 'priority',    'medium',           1),
    (25, 'environment', 'production',       1),
    (26, 'environment', 'staging',          1);

-- =====================================================
-- TMS TEST CASE ATTRIBUTES
-- =====================================================

INSERT INTO tms_test_case_attribute (test_case_id, attribute_id)
VALUES
    (4,  16),
    (5,  19),
    (7,  17),
    (8,  20),
    (9,  18),
    (10, 21),
    (13, 22),
    (15, 25),
    (22, 9),
    (23, 13),
    (24, 15),
    (25, 10),
    (36, 11),
    (37, 12),
    (37, 14);

-- =====================================================
-- TMS TEST CASE VERSIONS
-- =====================================================

INSERT INTO tms_test_case_version (id, test_case_id, name, is_default, is_draft)
VALUES
    (1,   4,   'Version 1',         true,  false),
    (2,   5,   'Version 1',         true,  false),
    (3,   6,   'Version 1',         true,  false),
    (4,   7,   'Version 1',         true,  false),
    (5,   8,   'Version 1',         true,  false),
    (6,   9,   'Version 1',         true,  false),
    (7,   10,  'Version 1',         true,  false),
    (8,   11,  'Version 1',         true,  false),
    (9,   12,  'Default Version',   true,  false),
    (10,  13,  'Default Version',   true,  false),
    (11,  14,  'Default Version',   true,  false),
    (12,  15,  'Default Version',   true,  false),
    (13,  16,  'Default Version',   true,  false),
    (14,  17,  'Default Version',   true,  false),
    (15,  18,  'Version 1.0',       true,  false),
    (16,  18,  'Version 2.0',       false, true),
    (17,  19,  'Default Version',   true,  false),
    (18,  20,  'Default Version',   true,  false),
    (19,  21,  'Default Version',   true,  false),
    (20,  22,  'Default Version',   true,  false),
    (21,  23,  'Default Version',   true,  false),
    (22,  24,  'Default Version',   true,  false),
    (23,  25,  'Default Version',   true,  false),
    (24,  26,  'Default Version',   true,  false),
    (25,  27,  'Default Version',   true,  false),
    (26,  28,  'Default Version',   true,  false),
    (27,  29,  'Default Version',   true,  false),
    (28,  30,  'Default Version',   true,  false),
    (29,  31,  'Default Version',   true,  false),
    (30,  32,  'Default Version',   true,  false),
    (31,  33,  'Default Version',   true,  false),
    (32,  34,  'Default Version',   true,  false),
    (33,  35,  'Default Version 35',true,  false),
    (34,  36,  'Default Version 34',true,  false),
    (35,  37,  'Default Version 36',true,  false),
    (100, 100, 'Default Version 100',true, false),
    (101, 101, 'Default Version 101',true, false),
    (102, 102, 'Default Version 102',true, false),
    (103, 103, 'Default Version 103',true, false);

-- =====================================================
-- TMS MANUAL SCENARIOS
-- =====================================================

INSERT INTO tms_manual_scenario (id, test_case_version_id, type, execution_estimation_time)
VALUES
    (1,  1,   'TEXT',  300),
    (2,  2,   'TEXT',  180),
    (3,  3,   'TEXT',  420),
    (4,  4,   'TEXT',  240),
    (5,  5,   'TEXT',  180),
    (6,  6,   'STEPS', 300),
    (7,  7,   'STEPS', 360),
    (8,  8,   'TEXT',  240),
    (9,  9,   'TEXT',  25),
    (10, 10,  'TEXT',  50),
    (11, 11,  'TEXT',  35),
    (12, 12,  'STEPS', 40),
    (13, 13,  'TEXT',  30),
    (14, 14,  'TEXT',  45),
    (15, 15,  'STEPS', 60),
    (16, 16,  'TEXT',  35),
    (17, 17,  'TEXT',  20),
    (18, 18,  'TEXT',  25),
    (19, 19,  'TEXT',  30),
    (20, 20,  'TEXT',  15),
    (21, 21,  'TEXT',  20),
    (22, 22,  'TEXT',  25),
    (23, 23,  'TEXT',  30),
    (24, 24,  'TEXT',  20),
    (25, 25,  'TEXT',  25),
    (26, 26,  'TEXT',  35),
    (27, 27,  'TEXT',  40),
    (28, 28,  'STEPS', 45),
    (29, 29,  'TEXT',  30),
    (30, 30,  'TEXT',  25),
    (31, 31,  'TEXT',  30),
    (32, 32,  'STEPS', 35);

-- =====================================================
-- TMS MANUAL SCENARIO REQUIREMENTS
-- =====================================================

INSERT INTO tms_manual_scenario_requirement (id, value, manual_scenario_id, number)
VALUES
    ('REQ-004', 'Requirement for TC4',  1,  0),
    ('REQ-005', 'Requirement for TC5',  2,  0),
    ('REQ-006', 'Requirement for TC6',  3,  0),
    ('REQ-007', 'Requirement for TC7',  4,  0),
    ('REQ-008', 'Requirement for TC8',  5,  0),
    ('REQ-009', 'Requirement for TC9',  6,  0),
    ('REQ-010', 'Requirement for TC10', 7,  0),
    ('REQ-011', 'Requirement for TC11', 8,  0),
    ('REQ-012', 'Requirement for TC12', 9,  0),
    ('REQ-013', 'Requirement for TC13', 10, 0),
    ('REQ-014', 'Requirement for TC14', 11, 0),
    ('REQ-015', 'Requirement for TC15', 12, 0),
    ('REQ-016', 'Requirement for TC16', 13, 0),
    ('REQ-017', 'Requirement for TC17', 14, 0),
    ('REQ-018', 'Requirement for TC18', 15, 0),
    ('REQ-019', 'Requirement for TC19', 16, 0),
    ('REQ-020', 'Requirement for TC20', 17, 0),
    ('REQ-021', 'Requirement for TC21', 18, 0),
    ('REQ-022', 'Requirement for TC22', 19, 0),
    ('REQ-023', 'Requirement for TC23', 20, 0),
    ('REQ-024', 'Requirement for TC24', 21, 0),
    ('REQ-025', 'Requirement for TC25', 22, 0),
    ('REQ-026', 'Requirement for TC26', 23, 0),
    ('REQ-027', 'Requirement for TC27', 24, 0),
    ('REQ-028', 'Requirement for TC28', 25, 0),
    ('REQ-029', 'Requirement for TC29', 26, 0),
    ('REQ-030', 'Requirement for TC30', 27, 0),
    ('REQ-031', 'Requirement for TC31', 28, 0),
    ('REQ-032', 'Requirement for TC32', 29, 0),
    ('REQ-033', 'Requirement for TC33', 30, 0),
    ('REQ-034', 'Requirement for TC34', 31, 0),
    ('REQ-035', 'Requirement for TC35', 32, 0);

-- =====================================================
-- TMS MANUAL SCENARIO PRECONDITIONS
-- =====================================================

INSERT INTO tms_manual_scenario_preconditions (id, manual_scenario_id, value)
VALUES
    (1,  1,  'System must be ready'),
    (2,  2,  'User must be logged in'),
    (3,  3,  'Test environment ready'),
    (4,  4,  'System initialized'),
    (5,  5,  'User authentication ready'),
    (6,  6,  'Basic setup complete'),
    (7,  7,  'High priority test setup'),
    (8,  8,  'Medium complexity setup'),
    (9,  9,  'Low priority setup'),
    (10, 10, 'Search functionality ready'),
    (11, 11, 'Search system initialized'),
    (12, 12, 'Login system ready'),
    (13, 13, 'API endpoints available'),
    (14, 14, 'Version control ready'),
    (15, 15, 'Complex scenario setup'),
    (16, 16, 'Draft version setup'),
    (17, 17, 'Full-text search ready'),
    (18, 18, 'Search indexing complete'),
    (19, 19, 'Search filters ready'),
    (20, 20, 'Batch processing ready'),
    (21, 21, 'Priority batch ready'),
    (22, 22, 'Tag management ready'),
    (23, 23, 'Tag processing ready'),
    (24, 24, 'Null field handling ready'),
    (25, 25, 'Null validation ready'),
    (26, 26, 'Attribute validation ready'),
    (27, 27, 'Invalid attribute handling'),
    (28, 28, 'Full attribute processing'),
    (29, 29, 'Complete attribute setup'),
    (30, 30, 'Folder validation ready'),
    (31, 31, 'Non-existing folder handling'),
    (32, 32, 'Final version processing');

-- =====================================================
-- TMS TEXT MANUAL SCENARIOS
-- =====================================================

INSERT INTO tms_text_manual_scenario (manual_scenario_id, instructions, expected_result)
VALUES
    (1,  'Execute test case 4 functionality',       'Test should pass with high priority'),
    (2,  'Execute test case 5 functionality',       'Test should pass with medium priority'),
    (3,  'Execute test case 6 functionality',       'Test should pass with low priority'),
    (4,  'Execute test case 7 functionality',       'System should respond correctly'),
    (5,  'Execute test case 8 functionality',       'Authentication should work'),
    (8,  'Execute test case 11 functionality',      'Medium complexity test should pass'),
    (9,  'Execute test case 12 functionality',      'Low priority test should complete'),
    (10, 'Execute search test case functionality',  'Search should return correct results'),
    (11, 'Execute another test functionality',      'System should handle the test'),
    (13, 'Execute API test case functionality',     'API endpoints should respond'),
    (14, 'Execute version test functionality',      'Version control should work'),
    (16, 'Execute draft version functionality',     'Draft should be processed'),
    (17, 'Execute full-text search test',           'Search indexing should work'),
    (18, 'Execute search indexing test',            'Indexing should complete'),
    (19, 'Execute search filter test',              'Filters should apply correctly'),
    (20, 'Execute batch test 22',                   'Batch processing should work'),
    (21, 'Execute batch test 23',                   'Priority batch should process'),
    (22, 'Execute tag management test',             'Tags should be managed correctly'),
    (23, 'Execute tag processing test',             'Tag processing should work'),
    (24, 'Execute null field test',                 'Null fields should be handled'),
    (25, 'Execute null validation test',            'Null validation should work'),
    (26, 'Execute attribute validation test',       'Attributes should be validated'),
    (27, 'Execute invalid attribute test',          'Invalid attributes should be handled'),
    (29, 'Execute complete attribute test',         'All attributes should process'),
    (30, 'Execute folder validation test',          'Folder validation should work'),
    (31, 'Execute non-existing folder test',        'Non-existing folders should be handled');

-- =====================================================
-- TMS STEPS MANUAL SCENARIOS
-- =====================================================

INSERT INTO tms_steps_manual_scenario (manual_scenario_id)
VALUES
    (6),
    (7),
    (12),
    (15),
    (28),
    (32);

-- =====================================================
-- TMS MANUAL SCENARIO ATTRIBUTES
-- =====================================================

INSERT INTO tms_manual_scenario_attribute (manual_scenario_id, attribute_id)
VALUES
    (1,  22),
    (2,  25),
    (10, 23),
    (12, 26),
    (15, 24);

-- =====================================================
-- TMS STEPS
-- =====================================================

INSERT INTO tms_step (id, steps_manual_scenario_id, instructions, expected_result, number)
VALUES
    (1,  6,  'Navigate to test case 6',              'Application should start',                1),
    (2,  6,  'Execute basic functionality',           'Basic functions should work',             2),
    (3,  6,  'Verify results',                        'Results should be correct',               3),
    (4,  7,  'Navigate to high priority test',        'High priority test should load',          1),
    (5,  7,  'Execute high priority actions',         'Actions should complete',                 2),
    (6,  7,  'Verify high priority results',          'Results should meet criteria',            3),
    (7,  12, 'Navigate to login page',                'Login page should appear',                1),
    (8,  12, 'Enter credentials',                     'Credentials should be accepted',          2),
    (9,  12, 'Verify login success',                  'User should be logged in',                3),
    (10, 15, 'Navigate to complex scenario',          'Complex scenario should load',            1),
    (11, 15, 'Execute complex operations',            'Operations should complete',              2),
    (12, 15, 'Verify complex results',                'All results should be correct',           3),
    (13, 28, 'Navigate to attribute processing',      'Attribute processing should start',       1),
    (14, 28, 'Execute full attribute test',           'All attributes should be processed',      2),
    (15, 28, 'Verify attribute results',              'Attribute processing should succeed',     3),
    (16, 32, 'Navigate to final version test',        'Final version should load',               1),
    (17, 32, 'Execute final processing',              'Final processing should complete',        2),
    (18, 32, 'Verify final results',                  'Final results should be correct',         3);

-- =====================================================
-- TICKETS (BTS integration)
-- =====================================================

INSERT INTO ticket (id, ticket_id, submitter, submit_date, bts_url, bts_project, url)
VALUES
    (1, 'JIRA-123', 'superadmin', NOW(), 'https://jira.example.com', 'PROJECT', 'https://jira.example.com/browse/JIRA-123'),
    (2, 'JIRA-456', 'superadmin', NOW(), 'https://jira.example.com', 'PROJECT', 'https://jira.example.com/browse/JIRA-456'),
    (3, 'JIRA-789', 'superadmin', NOW(), 'https://jira.example.com', 'PROJECT', 'https://jira.example.com/browse/JIRA-789');

-- =====================================================
-- TMS ATTACHMENTS
-- =====================================================

INSERT INTO tms_attachment (id, file_name, file_size, file_type, path_to_file, created_at, expires_at)
VALUES
    (1000, 'test-screenshot.png', 45678,  'image/png',        '/data/attachments/temp/1000.png', NOW(), NOW() + INTERVAL '24 hours'),
    (1001, 'error-log.txt',       2345,   'text/plain',        '/data/attachments/temp/1001.txt', NOW(), NOW() + INTERVAL '24 hours'),
    (1002, 'network-trace.har',   123456, 'application/json',  '/data/attachments/temp/1002.har', NOW(), NOW() + INTERVAL '24 hours');

-- =====================================================
-- LAUNCHES
-- MANUAL launches получают display_id (L-N, нумерация per project)
-- AUTOMATION launches: display_id = NULL
--
-- project 1 MANUAL: 100→L-1, 101→L-2, 102→L-3, 200→L-4, 201→L-5, 202→L-6, 203→L-7
-- project 2 MANUAL: 205→L-1
-- =====================================================

INSERT INTO launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries, rerun, approximate_duration, launch_type, display_id, test_plan_id, retention_policy)
VALUES
    (100, '550e8400-e29b-41d4-a716-446655440100', 1, 1, 'Launch for TC Execution Tests',          'Launch for testing last execution functionality',      '2023-10-06 10:00:00', '2023-10-06 11:00:00', 100, '2023-10-06 11:00:00', 'DEFAULT', 'PASSED',      false, false, 0,    'MANUAL',     'L-1', NULL, 'REGULAR'),
    (101, '550e8400-e29b-41d4-a716-446655440101', 1, 1, 'Launch for Multiple Executions',         'Launch for testing multiple executions',               '2023-10-07 10:00:00', '2023-10-07 11:00:00', 101, '2023-10-07 11:00:00', 'DEFAULT', 'PASSED',      false, false, 0,    'MANUAL',     'L-2', NULL, 'REGULAR'),
    (102, '550e8400-e29b-41d4-a716-446655440102', 1, 1, 'Launch for Latest Execution',            'Launch for testing latest execution logic',            '2023-10-08 10:00:00', '2023-10-08 11:00:00', 102, '2023-10-08 11:00:00', 'DEFAULT', 'PASSED',      false, false, 0,    'MANUAL',     'L-3', NULL, 'REGULAR'),
    (200, '550e8400-e29b-41d4-a716-446655440200', 1, 1, 'Manual Launch 1',                        'First manual launch with test plan',                   '2024-01-15 10:00:00', NULL,                  200, '2024-01-15 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0,    'MANUAL',     'L-4', 1,    'REGULAR'),
    (201, '550e8400-e29b-41d4-a716-446655440201', 1, 1, 'Manual Launch 2',                        'Second manual launch without test plan',               '2024-01-16 10:00:00', NULL,                  201, '2024-01-16 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0,    'MANUAL',     'L-5', NULL, 'REGULAR'),
    (202, '550e8400-e29b-41d4-a716-446655440202', 1, 1, 'Manual Launch for Deletion',             'Launch to be deleted in tests',                        '2024-01-17 10:00:00', NULL,                  202, '2024-01-17 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0,    'MANUAL',     'L-6', 1,    'REGULAR'),
    (203, '550e8400-e29b-41d4-a716-446655440203', 1, 1, 'Manual Launch with Multiple Executions', 'Launch with various test executions',                  '2024-01-18 10:00:00', NULL,                  203, '2024-01-18 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0,    'MANUAL',     'L-7', 2,    'REGULAR'),
    (204, '550e8400-e29b-41d4-a716-446655440204', 1, 1, 'Automation Launch 1',                    'Automated test run',                                   '2024-01-19 10:00:00', '2024-01-19 11:00:00', 204, '2024-01-19 11:00:00', 'DEFAULT', 'PASSED',      false, false, 3600, 'AUTOMATION', NULL,  NULL, 'REGULAR'),
    (205, '550e8400-e29b-41d4-a716-446655440205', 2, 2, 'Default Project Manual Launch',          'Manual launch in default project',                     '2024-01-20 10:00:00', NULL,                  205, '2024-01-20 10:00:00', 'DEFAULT', 'IN_PROGRESS', false, false, 0,    'MANUAL',     'L-1', 3,    'REGULAR');

-- =====================================================
-- LAUNCH ATTRIBUTES
-- =====================================================

INSERT INTO item_attribute (id, key, value, system, launch_id)
VALUES
    (200, 'environment', 'staging',   false, 200),
    (201, 'browser',     'chrome',    false, 200),
    (202, 'version',     '1.2.3',     false, 200),
    (203, 'team',        'qa-team-1', false, 201),
    (204, 'priority',    'high',      false, 201),
    (205, 'sprint',      'Sprint-25', false, 203),
    (206, 'release',     '2024.1',    false, 203);

-- =====================================================
-- LAUNCH STATISTICS
-- =====================================================

INSERT INTO statistics (s_id, s_counter, statistics_field_id, item_id, launch_id)
VALUES
    (2001, 2, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$total'),   NULL, 200),
    (2002, 1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$passed'),  NULL, 200),
    (2003, 1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$failed'),  NULL, 200),
    (2004, 0, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$skipped'), NULL, 200),
    (2005, 3, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$total'),   NULL, 201),
    (2006, 2, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$passed'),  NULL, 201),
    (2007, 0, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$failed'),  NULL, 201),
    (2008, 1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$skipped'), NULL, 201),
    (2009, 1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$total'),   NULL, 202),
    (2010, 0, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$passed'),  NULL, 202),
    (2011, 0, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$failed'),  NULL, 202),
    (2012, 0, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$skipped'), NULL, 202),
    (2013, 5, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$total'),   NULL, 203),
    (2014, 3, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$passed'),  NULL, 203),
    (2015, 1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$failed'),  NULL, 203),
    (2016, 1, (SELECT sf_id FROM statistics_field WHERE name = 'statistics$executions$skipped'), NULL, 203);

-- =====================================================
-- TEST ITEMS - SUITE (Folders)
-- =====================================================

INSERT INTO test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
VALUES
    (10001, '550e8400-e29b-41d4-a716-550000010001', 'Integration Tests', NULL, 'SUITE', '2023-10-06 10:00:00', NULL, '2023-10-06 10:00:00', '10001', 'launch-100-folder-3', NULL, true, false, true, NULL, NULL, 100, 9001001),
    (10002, '550e8400-e29b-41d4-a716-550000010002', 'Integration Tests', NULL, 'SUITE', '2023-10-07 10:00:00', NULL, '2023-10-07 10:00:00', '10002', 'launch-101-folder-3', NULL, true, false, true, NULL, NULL, 101, 9001002),
    (10003, '550e8400-e29b-41d4-a716-550000010003', 'Integration Tests', NULL, 'SUITE', '2023-10-08 10:00:00', NULL, '2023-10-08 10:00:00', '10003', 'launch-102-folder-3', NULL, true, false, true, NULL, NULL, 102, 9001003),
    (10004, '550e8400-e29b-41d4-a716-550000010004', 'Smoke Tests',       NULL, 'SUITE', '2024-01-15 10:05:00', NULL, '2024-01-15 10:05:00', '10004', 'launch-200-folder-1', NULL, true, false, true, NULL, NULL, 200, 9001004),
    (10005, '550e8400-e29b-41d4-a716-550000010005', 'Smoke Tests',       NULL, 'SUITE', '2024-01-16 10:05:00', NULL, '2024-01-16 10:05:00', '10005', 'launch-201-folder-1', NULL, true, false, true, NULL, NULL, 201, 9001005),
    (10006, '550e8400-e29b-41d4-a716-550000010006', 'Regression Tests',  NULL, 'SUITE', '2024-01-16 10:25:00', NULL, '2024-01-16 10:25:00', '10006', 'launch-201-folder-2', NULL, true, false, true, NULL, NULL, 201, 9001006),
    (10007, '550e8400-e29b-41d4-a716-550000010007', 'Integration Tests', NULL, 'SUITE', '2024-01-17 10:05:00', NULL, '2024-01-17 10:05:00', '10007', 'launch-202-folder-3', NULL, true, false, true, NULL, NULL, 202, 9001007),
    (10008, '550e8400-e29b-41d4-a716-550000010008', 'Smoke Tests',       NULL, 'SUITE', '2024-01-18 10:05:00', NULL, '2024-01-18 10:05:00', '10008', 'launch-203-folder-1', NULL, true, false, true, NULL, NULL, 203, 9001008),
    (10009, '550e8400-e29b-41d4-a716-550000010009', 'Regression Tests',  NULL, 'SUITE', '2024-01-18 10:40:00', NULL, '2024-01-18 10:40:00', '10009', 'launch-203-folder-2', NULL, true, false, true, NULL, NULL, 203, 9001009),
    (10010, '550e8400-e29b-41d4-a716-550000010010', 'Integration Tests', NULL, 'SUITE', '2024-01-18 10:55:00', NULL, '2024-01-18 10:55:00', '10010', 'launch-203-folder-3', NULL, true, false, true, NULL, NULL, 203, 9001010);

-- =====================================================
-- TMS TEST FOLDER TEST ITEM
-- =====================================================

INSERT INTO tms_test_folder_test_item (name, description, test_folder_id, launch_id, test_item_id)
VALUES
    ('Integration Tests', 'API and integration tests',    3, 100, 10001),
    ('Integration Tests', 'API and integration tests',    3, 101, 10002),
    ('Integration Tests', 'API and integration tests',    3, 102, 10003),
    ('Smoke Tests',       'Critical smoke test scenarios', 1, 200, 10004),
    ('Smoke Tests',       'Critical smoke test scenarios', 1, 201, 10005),
    ('Regression Tests',  'Full regression test suite',   2, 201, 10006),
    ('Integration Tests', 'API and integration tests',    3, 202, 10007),
    ('Smoke Tests',       'Critical smoke test scenarios', 1, 203, 10008),
    ('Regression Tests',  'Full regression test suite',   2, 203, 10009),
    ('Integration Tests', 'API and integration tests',    3, 203, 10010);

-- =====================================================
-- TEST ITEMS - TEST (Test Cases)
-- =====================================================

INSERT INTO test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, test_case_id, has_children, has_retries, has_stats, parent_id, retry_of, launch_id, test_case_hash)
VALUES
    (1000, '550e8400-e29b-41d4-a716-446655441000', 'Test Item for TC 100',                        'com.test.TC100', 'TEST', '2023-10-06 10:00:00', 'Test execution for test case 100',                  '2023-10-06 10:05:00', '10001.1000', 'tc-100-exec-1',        NULL, false, false, true, 10001, NULL, 100, 2839437),
    (1010, '550e8400-e29b-41d4-a716-446655441010', 'Test Item for TC 101 - First',                'com.test.TC101', 'TEST', '2023-10-05 10:00:00', 'First execution for test case 101',                 '2023-10-05 10:05:00', '10001.1010', 'tc-101-exec-1',        NULL, false, false, true, 10001, NULL, 100, 2839438),
    (1011, '550e8400-e29b-41d4-a716-446655441011', 'Test Item for TC 101 - Latest',               'com.test.TC101', 'TEST', '2023-10-07 14:00:00', 'Latest execution for test case 101',                '2023-10-07 14:05:00', '10002.1011', 'tc-101-exec-2',        NULL, false, false, true, 10002, NULL, 101, 2839439),
    (1030, '550e8400-e29b-41d4-a716-446655441030', 'Test Item for TC 103',                        'com.test.TC103', 'TEST', '2023-10-08 15:00:00', 'Test execution for test case 103',                  '2023-10-08 15:05:00', '10003.1030', 'tc-103-exec-1',        NULL, false, false, true, 10003, NULL, 102, 2839440),
    (2000, '550e8400-e29b-41d4-a716-446655442000', 'Execution: User Login with Valid Credentials','manual.tc4.exec1','TEST','2024-01-15 10:05:00', 'Manual execution of test case 4',                   '2024-01-15 10:10:00', '10004.2000', 'manual-launch-200-tc4-1',NULL,false, false, true, 10004, NULL, 200, 2839441),
    (2001, '550e8400-e29b-41d4-a716-446655442001', 'Execution: User Login with Invalid Credentials','manual.tc5.exec1','TEST','2024-01-15 10:15:00','Manual execution of test case 5',                  '2024-01-15 10:25:00', '10004.2001', 'manual-launch-200-tc5-1',NULL,false, false, true, 10004, NULL, 200, 2839442),
    (2002, '550e8400-e29b-41d4-a716-446655442002', 'Execution: Password Reset Flow',              'manual.tc6.exec1','TEST','2024-01-16 10:05:00', 'Manual execution of test case 6',                   '2024-01-16 10:20:00', '10005.2002', 'manual-launch-201-tc6-1',NULL,false, false, true, 10005, NULL, 201, 2839443),
    (2003, '550e8400-e29b-41d4-a716-446655442003', 'Execution: User Profile Update',              'manual.tc7.exec1','TEST','2024-01-16 10:25:00', 'Manual execution of test case 7',                   '2024-01-16 10:35:00', '10006.2003', 'manual-launch-201-tc7-1',NULL,false, false, true, 10006, NULL, 201, 2839444),
    (2004, '550e8400-e29b-41d4-a716-446655442004', 'Execution: Data Export Functionality',        'manual.tc8.exec1','TEST','2024-01-16 10:40:00', 'Manual execution of test case 8',                   '2024-01-16 10:50:00', '10006.2004', 'manual-launch-201-tc8-1',NULL,false, false, true, 10006, NULL, 201, 2839445),
    (2005, '550e8400-e29b-41d4-a716-446655442005', 'Execution: API Integration Test',             'manual.tc9.exec1','TEST','2024-01-17 10:05:00', 'Manual execution of test case 9',                   '2024-01-17 10:15:00', '10007.2005', 'manual-launch-202-tc9-1',NULL,false, false, true, 10007, NULL, 202, 2839446),
    (2006, '550e8400-e29b-41d4-a716-446655442006', 'Execution 1: User Login with Valid Credentials','manual.tc4.exec2','TEST','2024-01-18 10:05:00','First execution of TC4 in launch 203',              '2024-01-18 10:10:00', '10008.2006', 'manual-launch-203-tc4-1',NULL,false, false, true, 10008, NULL, 203, 2839447),
    (2007, '550e8400-e29b-41d4-a716-446655442007', 'Execution 2: User Login with Valid Credentials','manual.tc4.exec3','TEST','2024-01-18 10:15:00','Second execution of TC4 in launch 203',             '2024-01-18 10:20:00', '10008.2007', 'manual-launch-203-tc4-2',NULL,false, false, true, 10008, NULL, 203, 2839448),
    (2008, '550e8400-e29b-41d4-a716-446655442008', 'Execution 1: Password Reset Flow',            'manual.tc6.exec2','TEST','2024-01-18 10:25:00', 'First execution of TC6 in launch 203',              '2024-01-18 10:35:00', '10008.2008', 'manual-launch-203-tc6-1',NULL,false, false, true, 10008, NULL, 203, 2839449),
    (2009, '550e8400-e29b-41d4-a716-446655442009', 'Execution: User Profile Update',              'manual.tc7.exec2','TEST','2024-01-18 10:40:00', 'Execution of TC7 in launch 203',                   '2024-01-18 10:50:00', '10009.2009', 'manual-launch-203-tc7-1',NULL,false, false, true, 10009, NULL, 203, 2839450),
    (2010, '550e8400-e29b-41d4-a716-446655442010', 'Execution: Database Connection Test',         'manual.tc10.exec1','TEST','2024-01-18 10:55:00','Execution of TC10 in launch 203',                  '2024-01-18 11:00:00', '10010.2010', 'manual-launch-203-tc10-1',NULL,false,false, true, 10010, NULL, 203, 2839451);

-- =====================================================
-- TEST ITEM RESULTS (TEST items)
-- =====================================================

INSERT INTO test_item_results (result_id, status, end_time, duration)
VALUES
    (1000, 'PASSED',      '2023-10-06 10:05:00', 300000),
    (1010, 'PASSED',      '2023-10-05 10:05:00', 300000),
    (1011, 'PASSED',      '2023-10-07 14:05:00', 300000),
    (1030, 'PASSED',      '2023-10-08 15:05:00', 300000),
    (2000, 'PASSED',      '2024-01-15 10:10:00', 300000),
    (2001, 'FAILED',      '2024-01-15 10:25:00', 600000),
    (2002, 'PASSED',      '2024-01-16 10:20:00', 900000),
    (2003, 'PASSED',      '2024-01-16 10:35:00', 600000),
    (2004, 'SKIPPED',     '2024-01-16 10:50:00', 0),
    (2005, 'IN_PROGRESS', NULL,                  0),
    (2006, 'PASSED',      '2024-01-18 10:10:00', 300000),
    (2007, 'FAILED',      '2024-01-18 10:20:00', 300000),
    (2008, 'PASSED',      '2024-01-18 10:35:00', 600000),
    (2009, 'PASSED',      '2024-01-18 10:50:00', 600000),
    (2010, 'SKIPPED',     '2024-01-18 11:00:00', 0);

-- =====================================================
-- TEST ITEM RESULTS (SUITE items)
-- =====================================================

INSERT INTO test_item_results (result_id, status, end_time, duration)
VALUES
    (10001, 'PASSED',      '2023-10-06 10:05:00', 600000),
    (10002, 'PASSED',      '2023-10-07 14:05:00', 300000),
    (10003, 'PASSED',      '2023-10-08 15:05:00', 300000),
    (10004, 'FAILED',      '2024-01-15 10:25:00', 900000),
    (10005, 'PASSED',      '2024-01-16 10:20:00', 900000),
    (10006, 'PASSED',      '2024-01-16 10:50:00', 1200000),
    (10007, 'IN_PROGRESS', NULL,                  0),
    (10008, 'FAILED',      '2024-01-18 10:35:00', 1800000),
    (10009, 'PASSED',      '2024-01-18 10:50:00', 600000),
    (10010, 'SKIPPED',     '2024-01-18 11:00:00', 0);

-- =====================================================
-- TMS TEST CASE EXECUTIONS
-- =====================================================

INSERT INTO tms_test_case_execution (id, test_case_id, test_case_version_id, test_item_id, test_case_snapshot, launch_id)
VALUES
    (1,  100, 100, 1000, '{"id": 100, "name": "Test Case with Last Execution", "priority": "HIGH"}', 100),
    (2,  101, 101, 1010, '{"id": 101, "name": "Test Case with Multiple Executions", "priority": "MEDIUM"}', 100),
    (3,  101, 101, 1011, '{"id": 101, "name": "Test Case with Multiple Executions", "priority": "MEDIUM"}', 101),
    (4,  103, 103, 1030, '{"id": 103, "name": "Test Case with Single Execution", "priority": "HIGH"}', 102),
    (10, 4,   1,   2000, '{"id": 4, "name": "User Login with Valid Credentials", "priority": "CRITICAL", "description": "Verify user can login with valid username and password", "folder": {"id": 1, "name": "Smoke Tests"}}', 200),
    (11, 5,   2,   2001, '{"id": 5, "name": "User Login with Invalid Credentials", "priority": "HIGH", "description": "Verify appropriate error message for invalid credentials", "folder": {"id": 1, "name": "Smoke Tests"}}', 200),
    (12, 6,   3,   2002, '{"id": 6, "name": "Password Reset Flow", "priority": "MEDIUM", "description": "Verify complete password reset functionality", "folder": {"id": 1, "name": "Smoke Tests"}}', 201),
    (13, 7,   4,   2003, '{"id": 7, "name": "User Profile Update", "priority": "MEDIUM", "description": "Verify user can update profile information", "folder": {"id": 2, "name": "Regression Tests"}}', 201),
    (14, 8,   5,   2004, '{"id": 8, "name": "Data Export Functionality", "priority": "LOW", "description": "Verify data can be exported in CSV format", "folder": {"id": 2, "name": "Regression Tests"}}', 201),
    (15, 9,   6,   2005, '{"id": 9, "name": "API Integration Test", "priority": "HIGH", "description": "Verify REST API endpoints respond correctly", "folder": {"id": 3, "name": "Integration Tests"}}', 202),
    (16, 4,   1,   2006, '{"id": 4, "name": "User Login with Valid Credentials", "priority": "CRITICAL", "description": "Verify user can login with valid username and password", "folder": {"id": 1, "name": "Smoke Tests"}}', 203),
    (17, 4,   1,   2007, '{"id": 4, "name": "User Login with Valid Credentials", "priority": "CRITICAL", "description": "Verify user can login with valid username and password", "folder": {"id": 1, "name": "Smoke Tests"}}', 203),
    (18, 6,   3,   2008, '{"id": 6, "name": "Password Reset Flow", "priority": "MEDIUM", "description": "Verify complete password reset functionality", "folder": {"id": 1, "name": "Smoke Tests"}}', 203),
    (19, 7,   4,   2009, '{"id": 7, "name": "User Profile Update", "priority": "MEDIUM", "description": "Verify user can update profile information", "folder": {"id": 2, "name": "Regression Tests"}}', 203),
    (20, 10,  7,   2010, '{"id": 10, "name": "Database Connection Test", "priority": "CRITICAL", "description": "Verify database connectivity and queries", "folder": {"id": 3, "name": "Integration Tests"}}', 203);

-- =====================================================
-- TMS TEST CASE EXECUTION COMMENTS
-- =====================================================

INSERT INTO tms_test_case_execution_comment (id, execution_id, comment)
VALUES
    (1, 10, 'Test passed successfully on first attempt. All login validations working as expected.'),
    (2, 11, 'Test failed due to unexpected error message. Expected "Invalid credentials" but got "System error".'),
    (3, 12, 'Password reset email received within 2 minutes. All steps completed successfully.'),
    (4, 17, 'Second execution also failed. Login button not responding on Safari browser.');

-- =====================================================
-- TMS TEST CASE EXECUTION COMMENT BTS TICKETS
-- =====================================================

INSERT INTO tms_test_case_execution_comment_bts_ticket (id, comment_id, url)
VALUES
    (1, 2, 'https://jira.example.com/browse/JIRA-123'),
    (2, 4, 'https://jira.example.com/browse/JIRA-456');

-- =====================================================
-- TMS TEST CASE EXECUTION COMMENT ATTACHMENTS
-- =====================================================

INSERT INTO tms_test_case_execution_comment_attachment (execution_comment_id, attachment_id)
VALUES
    (2, 1000),
    (4, 1001),
    (4, 1002);

UPDATE tms_attachment SET expires_at = NULL WHERE id IN (1000, 1001, 1002);

-- =====================================================
-- TMS PROJECT SEQUENCES
-- Фиксируем текущие значения счётчиков display_id
-- =====================================================

INSERT INTO tms_project_sequence (project_id, entity_type, current_value)
VALUES
    (1, 'TEST_CASE', 32),  -- TC-1..TC-32 для project 1
    (2, 'TEST_CASE', 6),   -- TC-1..TC-6  для project 2
    (1, 'TEST_PLAN', 5),   -- TP-1..TP-5  для project 1
    (2, 'TEST_PLAN', 1),   -- TP-1        для project 2
    (1, 'LAUNCH',    7),   -- L-1..L-7    для project 1 (только MANUAL)
    (2, 'LAUNCH',    1);   -- L-1         для project 2

-- =====================================================
-- RESET SEQUENCES
-- =====================================================
SELECT setval('statistics_field_sf_id_seq',                        (SELECT COALESCE(MAX(sf_id), 1) FROM statistics_field));
SELECT setval('tms_test_folder_id_seq',                            (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_plan_id_seq',                              (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
SELECT setval('tms_test_case_id_seq',                              (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_case_version_id_seq',                      (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
SELECT setval('tms_manual_scenario_id_seq',                        (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario));
SELECT setval('tms_manual_scenario_preconditions_id_seq',          (SELECT COALESCE(MAX(id), 1) FROM tms_manual_scenario_preconditions));
SELECT setval('tms_step_id_seq',                                   (SELECT COALESCE(MAX(id), 1) FROM tms_step));
SELECT setval('tms_attribute_id_seq',                              (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
SELECT setval('ticket_id_seq',                                     (SELECT COALESCE(MAX(id), 1) FROM ticket));
SELECT setval('tms_attachment_id_seq',                             (SELECT COALESCE(MAX(id), 1) FROM tms_attachment));
SELECT setval('launch_id_seq',                                     (SELECT COALESCE(MAX(id), 1) FROM launch));
SELECT setval('item_attribute_id_seq',                             (SELECT COALESCE(MAX(id), 1) FROM item_attribute));
SELECT setval('statistics_s_id_seq',                               (SELECT COALESCE(MAX(s_id), 1) FROM statistics));
SELECT setval('test_item_item_id_seq',                             (SELECT COALESCE(MAX(item_id), 1) FROM test_item));
SELECT setval('tms_test_case_execution_id_seq',                    (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_execution));
SELECT setval('tms_test_case_execution_comment_id_seq',            (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_execution_comment));
SELECT setval('tms_test_case_execution_comment_bts_ticket_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_execution_comment_bts_ticket));
SELECT setval('tms_test_folder_test_item_id_seq',                  (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder_test_item));
