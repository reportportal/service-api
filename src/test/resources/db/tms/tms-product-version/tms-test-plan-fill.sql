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

-- Test folders for test cases
insert into tms_test_folder (id, "name", project_id)
values (7, 'Test Folder 1', 1),
       (8, 'Test Folder 2', 1),
       (9, 'Test Folder 3', 1);

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

-- Update sequences
SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
