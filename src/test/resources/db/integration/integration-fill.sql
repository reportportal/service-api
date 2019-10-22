INSERT INTO integration(id, project_id, type, enabled, params, creator, creation_date, name)
VALUES (7, NULL, 2, TRUE, '{
  "param": "value"
}', 'superadmin', now(), 'name1'),
       (8, 2, 2, TRUE, '{
         "param": "value"
       }', 'superadmin', now(), 'name2');

ALTER SEQUENCE integration_id_seq RESTART WITH 9;