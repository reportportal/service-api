-- Insert Organization Data
insert into organization (id, name, slug, organization_type)
values (101, 'test Org', 'org', 'INTERNAL');

-- Insert Project Data
insert into project (id, name, organization, organization_id, created_at)
values (31, 'test_project31', 'org', 101, now());

insert into project (id, name, organization, organization_id, created_at)
values (32, 'test_project32', 'org', 101, now());

-- Insert Dataset Data
insert into tms_dataset (id, name, project_id)
values (10001, 'Dataset10001', 31);

insert into tms_dataset (id, name, project_id)
values (10002, 'Dataset10002', 31);
