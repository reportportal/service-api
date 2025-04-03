insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (3, 'test_personal' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_product_version (id, documentation, "version", project_id)
values (3,'documentation3', 'version3', 3);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (3, 'milestone3', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type3', 3);

insert into tms_environment (id, "name", project_id)
values (3, 'name3', 3);

insert into tms_attribute (id, "key")
values (3, 'value3');



insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (4, 'test_personal4' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_product_version (id, documentation, "version", project_id)
values (4,'documentation4', 'version4', 4);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (4, 'milestone4', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type4', 4);

insert into tms_environment (id, "name", project_id)
values (4, 'name4', 4);

insert into tms_attribute (id, "key")
values (4, 'value4');

insert into tms_test_plan (id, "name" , description, project_id, environment_id, product_version_id)
values (4,'name4', 'description4', 4, 4, 4);


insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (5, 'test_personal5' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_product_version (id, documentation, "version", project_id)
values (5,'documentation5', 'version5', 5);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (5, 'milestone5', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type4', 5);

insert into tms_environment (id, "name", project_id)
values (5, 'name5', 5);

insert into tms_attribute (id, "key")
values (5, 'value5');

insert into tms_test_plan (id, "name" , description, project_id, environment_id, product_version_id)
values (5,'name5', 'description5', 5, 5, 5);


insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (6, 'test_personal6' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into tms_product_version (id, documentation, "version", project_id)
values (6,'documentation6', 'version6', 6);

insert into tms_milestone (id, "name", start_date, end_date, "type", product_version_id)
values (6, 'milestone6', '2025-02-17 16:07:59.076', '2025-02-17 16:07:59.076', 'type6', 6);

insert into tms_environment (id, "name", project_id)
values (6, 'name6', 6);

insert into tms_attribute (id, "key")
values (6, 'value6');

insert into tms_test_plan (id, "name" , description, project_id, environment_id, product_version_id)
values (6,'name6', 'description6', 6, 6, 6);
