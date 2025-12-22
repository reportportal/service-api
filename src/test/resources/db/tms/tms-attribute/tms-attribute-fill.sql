INSERT INTO tms_attribute (id, key, project_id)
VALUES (1, 'test_key_1', 1),
       (2, 'test_key_2', 1),
       (3, 'test_key_3', 1),
       (4, 'priority', 1),
       (5, 'component', 1),
       (6, 'browser', 1),
       (7, 'environment', 1),
       (8, 'regression', 1),
       (9, 'smoke', 1),
       (10, 'api', 1),
       (20, 'other_project_key_1', 1),
       (21, 'other_project_key_2', 1);

SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
