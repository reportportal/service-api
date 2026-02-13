-- Attributes with NULL value (tags)
INSERT INTO tms_attribute (id, key, value, project_id)
VALUES
    (1, 'test_key_1', NULL, 1),
    (2, 'test_key_2', NULL, 1),
    (3, 'test_key_3', NULL, 1),
    (4, 'smoke', NULL, 1),
    (5, 'regression', NULL, 1),
    (6, 'api', NULL, 1),

    -- Attributes with value (key-value pairs)
    (7, 'priority', 'high', 1),
    (8, 'priority', 'medium', 1),
    (9, 'priority', 'low', 1),
    (10, 'browser', 'chrome', 1),
    (11, 'browser', 'firefox', 1),
    (12, 'environment', 'prod', 1);

SELECT setval('tms_attribute_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tms_attribute));
