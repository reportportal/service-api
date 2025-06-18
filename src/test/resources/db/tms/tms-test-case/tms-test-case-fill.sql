insert into tms_test_folder (id, "name", description, project_id)
values (3, 'Test Folder 3', 'Description for test folder 3', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (4, 'Test Folder 4', 'Description for test folder 4', 1);

insert into tms_test_case (id, "name", description, test_folder_id)
values (4, 'Test Case 4', 'Description for test case 4', 4);

insert into tms_test_folder (id, "name", description, project_id)
values (5, 'Test Folder 5', 'Description for test folder 5', 1);

insert into tms_test_case (id, "name", description, test_folder_id)
values (5, 'Test Case 5', 'Description for test case 5', 5);

insert into tms_test_folder (id, "name", description, project_id)
values (6, 'Test Folder 6', 'Description for test folder 6', 1);

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

insert into tms_test_case (id, "name", description, test_folder_id)
values (7, 'Test Case 7', 'Description for test case 7', 4);

insert into tms_test_case (id, "name", description, test_folder_id)
values (8, 'Test Case 8', 'Description for test case 8', 5);

insert into tms_test_case (id, "name", description, test_folder_id)
values (9, 'Test Case 9', 'Description for test case 9', 4);

insert into tms_test_case (id, "name", description, test_folder_id)
values (10, 'Test Case 10', 'Description for test case 10', 5);

insert into tms_test_case (id, "name", description, test_folder_id)
values (11, 'Test Case 11', 'Description for test case 11', 5);

insert into tms_test_case (id, "name", description, test_folder_id)
values (12, 'Test Case 12', 'Description for test case 12', 5);

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
