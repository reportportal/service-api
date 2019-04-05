insert into integration(id, project_id, type, enabled, params, creation_date)
values (7, null, 2, true, '{"param":  "value"}', now()),
       (8, 2, 2, true, '{"param":"value"}', now());

alter sequence integration_id_seq restart with 9;