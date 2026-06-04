INSERT INTO ticket (id, ticket_id, submitter, submit_date, bts_url, bts_project, url)
VALUES (1, 'ticket_id_1', 'superadmin', now(), 'jira.com', 'project',
        'http://example.com/tickets/ticket_id_1'),
       (2, 'ticket_id_2', 'superadmin', now() - INTERVAL '2 day', 'jira.com', 'project',
        'http://example.com/tickets/ticket_id_2'),
       (3, 'ticket_id_3', 'superadmin', now() - INTERVAL '4 day', 'jira.com', 'project',
        'http://example.com/tickets/ticket_id_3');

INSERT INTO launch(uuid, project_id, user_id, name, start_time, last_modified, mode, status)
VALUES ('uuid', 1, 1, 'launch', now(), now(), 'DEFAULT', 'FAILED');

INSERT INTO test_item(test_case_hash, uuid, type, start_time, last_modified, launch_id)
VALUES (1, 'uuid', 'STEP', now(), now(), (SELECT currval(pg_get_serial_sequence('launch', 'id'))));

INSERT INTO test_item_results(result_id, status)
VALUES ((SELECT currval(pg_get_serial_sequence('test_item', 'item_id'))), 'FAILED');

INSERT INTO issue(issue_id, issue_type)
VALUES ((SELECT currval(pg_get_serial_sequence('test_item', 'item_id'))), 2);

INSERT INTO issue_ticket(issue_id, ticket_id)
VALUES ((SELECT currval(pg_get_serial_sequence('test_item', 'item_id'))), 1);