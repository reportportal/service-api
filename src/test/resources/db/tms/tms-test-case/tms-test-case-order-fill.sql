-- Custom order: folder 60 with four test cases without a position, folder 61 with one
insert into tms_test_folder (id, "name", description, project_id)
values (60, 'Order Folder', 'Folder for custom order tests', 1);

insert into tms_test_folder (id, "name", description, project_id)
values (61, 'Order Folder 2', 'Second folder for custom order tests', 1);

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (600, 'Order Case A', 'Order case A', 60, 'HIGH', 1, 'TC-600');

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (601, 'Order Case B', 'Order case B', 60, 'LOW', 1, 'TC-601');

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (602, 'Order Case C', 'Order case C', 60, 'MEDIUM', 1, 'TC-602');

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (603, 'Order Case D', 'Order case D', 60, 'BLOCKER', 1, 'TC-603');

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (604, 'Order Case E', 'Order case E', 61, 'LOW', 1, 'TC-604');

insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (600, 600, 'Default Version', true, false),
       (601, 601, 'Default Version', true, false),
       (602, 602, 'Default Version', true, false),
       (603, 603, 'Default Version', true, false),
       (604, 604, 'Default Version', true, false);

SELECT setval('tms_test_folder_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_folder));
SELECT setval('tms_test_case_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case));
SELECT setval('tms_test_case_version_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_test_case_version));
