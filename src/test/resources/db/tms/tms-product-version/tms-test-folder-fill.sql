insert into organization (id, name, slug, organization_type)
values (101, 'test Org', 'org', 'INTERNAL');

insert into project (id, name, organization, organization_id, created_at)
values (31, 'test_project31', 'org', 101, now());

insert into tms_test_folder (id, project_id, description, "name")
values (3, 31, 'description_folder3', 'name_folder3');

insert into tms_test_folder (id, project_id, description, "name")
values (4, 31, 'description_folder4', 'name_folder4');

insert into project (id, name, organization, organization_id, created_at)
values (35, 'test_project35', 'org', 101, now());

insert into tms_test_folder (id, project_id, description, "name")
values (5, 35, 'description_folder5', 'name_folder5');
