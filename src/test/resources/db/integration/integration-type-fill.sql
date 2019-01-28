INSERT INTO integration_type (id, name, auth_flow, creation_date, group_type)
VALUES (1, 'test integration type', 'LDAP', now(), 'AUTH'),                  --integration type id = 1
       (2, 'RALLY', 'OAUTH', now(), 'BTS'),                                         --integration type id = 2
       (3, 'JIRA', 'BASIC', now(), 'BTS'),                                          --integration type id = 3
       (4, 'EMAIL', null, now(), 'NOTIFICATION');                                   --integration type id = 4
