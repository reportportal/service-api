INSERT INTO tms_attribute (id, key)
VALUES (1, 'test_key_1'),
       (2, 'test_key_2'),
       (3, 'test_key_3'),
       (4, 'priority'),
       (5, 'component'),
       (6, 'browser'),
       (7, 'environment'),
       (8, 'regression'),
       (9, 'smoke'),
       (10, 'api'),
       (20, 'other_project_key_1'),
       (21, 'other_project_key_2');

SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
