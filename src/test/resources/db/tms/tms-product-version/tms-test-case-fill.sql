insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (3, 'test_personal' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_test_folder (id, project_id, description, "name")
values (3, 3, 'description_folder3', 'name_folder3');

insert into tms_dataset (id, name, project_id)
values (3, 'testDataset', 3);

insert into tms_test_case (id, test_folder_id, dataset_id, description, "name")
values (3, 3, 3, 'description3', 'name3');

insert into tms_test_case_version (is_default, is_draft, id, test_case_id, "name")
values (true, true, 3, 3, 'name_version3');
