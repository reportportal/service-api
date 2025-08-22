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

SELECT setval('tms_test_plan_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_plan));
