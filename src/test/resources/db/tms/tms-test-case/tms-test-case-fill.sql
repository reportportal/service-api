insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (3, 'test_personal3', 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_test_folder (id, "name", description, project_id)
values (3, 'Test Folder 3', 'Description for test folder 3', 3);

insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (4, 'test_personal4', 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_test_folder (id, "name", description, project_id)
values (4, 'Test Folder 4', 'Description for test folder 4', 4);

insert into tms_test_case (id, "name", description, test_folder_id)
values (4, 'Test Case 4', 'Description for test case 4', 4);

insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (5, 'test_personal5', 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_test_folder (id, "name", description, project_id)
values (5, 'Test Folder 5', 'Description for test folder 5', 5);

insert into tms_test_case (id, "name", description, test_folder_id)
values (5, 'Test Case 5', 'Description for test case 5', 5);

insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (6, 'test_personal6', 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

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
