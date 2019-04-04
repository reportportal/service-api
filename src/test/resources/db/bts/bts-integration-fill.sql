insert into integration(id, project_id, type, enabled, params, creation_date)
values (9, null, 4, true, '{"param":  "value"}', now()),
       (10, 1, 4, true, '{"params": {"url": "jira.com", "project": "project"}}', now());

alter sequence integration_id_seq restart with 11;