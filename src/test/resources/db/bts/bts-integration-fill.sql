insert into integration(id, project_id, type, enabled, params, creator, creation_date, name)
values (9, null, 4, true, '{
  "param": "value"
}', 'superadmin', now().'first name'),
       (10, 1, 4, true, '{
         "params": {
           "url": "jira.com",
           "project": "project"
         }
       }', 'superadmin', now(), 'second name');

alter sequence integration_id_seq restart with 11;