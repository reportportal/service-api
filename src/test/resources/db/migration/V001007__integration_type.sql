INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type)
VALUES (TRUE, 'rally', 'OAUTH', now(), 'BTS');
INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type)
VALUES (TRUE, 'jira', 'BASIC', now(), 'BTS');
INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type, details)
VALUES (TRUE, 'signup', null, now(), 'OTHER', '{"details": {"accessType": "public"}}');
