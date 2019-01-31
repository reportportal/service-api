INSERT INTO integration_type (id, name, auth_flow, creation_date, group_type, enabled)
VALUES (1, 'test integration type', 'LDAP', now(), 'AUTH', true),                  --integration type id = 1
       (2, 'RALLY', 'OAUTH', now(), 'BTS', true),                                         --integration type id = 2
       (3, 'JIRA', 'BASIC', now(), 'BTS', true),                                          --integration type id = 3
       (4, 'EMAIL', null, now(), 'NOTIFICATION', true);                                   --integration type id = 4
