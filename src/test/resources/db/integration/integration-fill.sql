INSERT INTO integration(id, project_id, type, enabled, params, creation_date)
VALUES (7, NULL, 2, TRUE, '{"param":  "value"}', now()),
       (8, 2, 2, TRUE, '{"param":"value"}', now());

ALTER SEQUENCE integration_id_seq RESTART WITH 9;