insert into organization (id, name, slug, organization_type)
values (101, 'test Org', 'org', 'INTERNAL');

insert into project (id, name, organization, organization_id, created_at)
values (3, 'test_project3', 'org', 101, now());

insert into tms_test_folder (id, "name", description, project_id)
values (3, 'Test Folder 3', 'Description for test folder 3', 3);

insert into project (id, name, organization, organization_id, created_at)
values (4, 'test_project4', 'org', 101, now());

insert into tms_test_folder (id, "name", description, project_id)
values (4, 'Test Folder 4', 'Description for test folder 4', 4);

insert into tms_test_case (id, "name", description, test_folder_id)
values (4, 'Test Case 4', 'Description for test case 4', 4);

insert into project (id, name, organization, organization_id, created_at)
values (5, 'test_project5', 'org', 101, now());

insert into tms_test_folder (id, "name", description, project_id)
values (5, 'Test Folder 5', 'Description for test folder 5', 5);

insert into tms_test_case (id, "name", description, test_folder_id)
values (5, 'Test Case 5', 'Description for test case 5', 5);

insert into project (id, name, organization, organization_id, created_at)
values (6, 'test_project6', 'org', 101, now());

insert into tms_test_folder (id, "name", description, project_id)
values (6, 'Test Folder 6', 'Description for test folder 6', 6);

insert into tms_test_case (id, "name", description, test_folder_id)
values (6, 'Test Case 6', 'Description for test case 6', 6);

insert into tms_attribute (id, "key")
values (3, 'test3');

insert into tms_attribute (id, "key")
values (4, 'test4');

insert into tms_attribute (id, "key")
values (5, 'test5');

insert into tms_attribute (id, "key")
values (6, 'test6');
