INSERT INTO integration (id, project_id, organization_id, type, enabled, params, creator, creation_date, name)
SELECT 901, NULL, 201, id, TRUE, '{"params": {"url": "http://jira.example.com", "project": "RP"}}',
       'admin@example.com', NOW(), 'jira-org-integration'
FROM integration_type WHERE name = 'jira';

INSERT INTO integration (id, project_id, organization_id, type, enabled, params, creator, creation_date, name)
SELECT 902, NULL, 201, id, TRUE, '{"params": {"url": "http://jira2.example.com", "project": "RP2"}}',
       'admin@example.com', NOW(), 'jira-org-integration-2'
FROM integration_type WHERE name = 'jira';

INSERT INTO integration (id, project_id, organization_id, type, enabled, params, creator, creation_date, name)
SELECT 903, NULL, 202, id, TRUE, '{"params": {"url": "http://jira3.example.com", "project": "RP3"}}',
       'admin@example.com', NOW(), 'jira-org-202-integration'
FROM integration_type WHERE name = 'jira';

ALTER SEQUENCE integration_id_seq RESTART WITH 910;
