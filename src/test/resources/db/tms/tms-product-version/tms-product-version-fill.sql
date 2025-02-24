insert into tms_product_version (id, documentation, "version")
values (3,'documentation3', 'version3');

insert into tms_environment (id, product_version_id)
values (1, 3);

insert into tms_test_plan (environment_id, id, product_version_id)
values (1, 1, 3);

insert into tms_product_version (id, documentation, "version")
values (4,'documentation4', 'version4');

insert into tms_product_version (id, documentation, "version")
values (5,'documentation5', 'version5');
