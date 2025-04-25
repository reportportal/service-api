ALTER SEQUENCE IF EXISTS tms_test_folder_id_seq RESTART WITH 1;

INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (3, 1, 'description_folder3', 'name_folder3');

INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (4, 1, 'description_folder4', 'name_folder4');

INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (5, 1, 'description_folder5', 'name_folder5');

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (6, 1, 'description_subfolder1', 'name_subfolder1', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (7, 1, 'description_subfolder2', 'name_subfolder2', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (8, 1, 'description_subsubfolder', 'name_subsubfolder', 6);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (9, 1, 'description_subfolder_of_4', 'name_subfolder_of_4', 4);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (10, 1, 'This is a longer description with special characters: !@#$%^&*()',
        'Export Test Folder', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (11, 1, NULL, 'Empty Description Folder', 3);

INSERT INTO tms_test_folder (id, project_id, description, "name")
VALUES (12, 1, 'Root folder for export testing', 'Export Root');

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (13, 1, 'First level subfolder 1', 'Export Sub 1', 12);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (14, 1, 'First level subfolder 2', 'Export Sub 2', 12);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (15, 1, 'Second level subfolder 1', 'Export Sub-Sub 1', 13);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (16, 1, 'Second level subfolder 2', 'Export Sub-Sub 2', 13);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (17, 1, 'Second level subfolder 3', 'Export Sub-Sub 3', 14);

INSERT INTO tms_test_folder (id, project_id, description, "name", parent_id)
VALUES (18, 1, 'Third level subfolder', 'Export Sub-Sub-Sub', 15);
