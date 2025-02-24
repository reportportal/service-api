insert into tms_test_folder (id, project_id, description, "name")
values (3, 3, 'description_folder3', 'name_folder3');

insert into tms_test_case (id, test_folder_id, description, "name")
values (3, 3, 'description3', 'name3');

insert into tms_test_case_version (is_default, is_draft, id, test_case_id, "name")
values (true, true, 3, 3, 'name_version3');