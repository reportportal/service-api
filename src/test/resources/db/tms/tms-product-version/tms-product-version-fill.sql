insert into organization (id, name, slug, organization_type)
values (101, 'test Org', 'org', 'INTERNAL');

insert into project (id, name, organization, organization_id, created_at)
values (3, 'test_project3', 'org', 101, now());

insert into tms_product_version (id, documentation, "version", project_id)
values (3,'documentation3', 'version3', 3);

insert into project (id, name, organization, organization_id, created_at)
values (4, 'test_project4', 'org', 101, now());

insert into tms_product_version (id, documentation, "version", project_id)
values (4,'documentation4', 'version4', 4);

insert into project (id, name, organization, organization_id, created_at)
values (5, 'test_project5', 'org', 101, now());

insert into tms_product_version (id, documentation, "version", project_id)
values (5,'documentation5', 'version5', 5);

