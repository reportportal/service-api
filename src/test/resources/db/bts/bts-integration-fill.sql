insert into integration(id, project_id, type, enabled, params, creator, creation_date, name)
values (9, null, 6, true, '{
  "param": "value"
}', 'admin@reportportal.internal', now(), 'first name'),
       (10, 1, 6, true, '{
         "params": {
           "url": "jira.com",
           "project": "project"
         }
       }', 'admin@reportportal.internal', now(), 'second name');

alter sequence integration_id_seq restart with 11;