insert into integration(id, project_id, type, enabled, params, creation_date)
values (9, null, 3, true, '{"param":  "value"}', now()),
       (10, 1, 3, true, '{"params": {"url": "jira.com", "project": "project"}}', now());

alter sequence integration_id_seq restart with 11;