CREATE OR REPLACE FUNCTION widget_content_init()
    RETURNS VOID AS
$$
    DECLARE launch1 BIGINT;
    DECLARE launch2 BIGINT;
    DECLARE launch3 BIGINT;
    DECLARE launch4 BIGINT;
    DECLARE itemId  BIGINT;
BEGIN

    alter sequence launch_id_seq restart with 1;
    alter sequence shareable_entity_id_seq restart with 1;
    alter sequence item_attribute_id_seq restart with 1;
    alter sequence test_item_item_id_seq restart with 1;
    alter sequence ticket_id_seq restart with 1;
    alter sequence activity_id_seq restart with 1;

    INSERT INTO public.owned_entity (id, owner, project_id)
    VALUES (1, 'superadmin', 1);
    INSERT INTO public.filter (id, name, target, description)
    VALUES (1, 'filter name', 'Launch', 'filter for product status widget');

    INSERT INTO public.launch (uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
    VALUES ('aa848441-72a1-4192-a828-cd20b7fcbd31',
            1,
            1,
            'launch name 1',
            null,
            '2018-11-22 12:46:14.198123',
            '2018-11-21 12:46:14.756123',
            1,
            '2018-11-21 15:46:18.423123',
            'DEFAULT',
            'FAILED');
    launch1 = (SELECT currval(pg_get_serial_sequence('launch', 'id')));

    INSERT INTO public.launch (uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
    VALUES ('aa848441-72a1-4192-a828-cd20b7fcbd32',
            1,
            1,
            'launch name 1',
            null,
            '2018-11-23 12:46:14.198123',
            '2018-11-21 12:46:14.756123',
            2,
            '2018-11-21 15:46:18.423123',
            'DEFAULT',
            'FAILED');
    launch2 = (SELECT currval(pg_get_serial_sequence('launch', 'id')));

    INSERT INTO public.launch (uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
    VALUES ('aa848441-72a1-4192-a828-cd20b7fcbd33',
            1,
            1,
            'launch name 1',
            null,
            '2018-11-24 12:46:14.198123',
            '2018-11-21 12:46:14.756123',
            3,
            '2018-11-21 15:46:18.423123',
            'DEFAULT',
            'FAILED');
    launch3 = (SELECT currval(pg_get_serial_sequence('launch', 'id')));

    INSERT INTO public.launch (uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
    VALUES ('aa848441-72a1-4192-a828-cd20b7fcbd34',
            1,
            1,
            'launch name 1',
            null,
            '2018-11-25 12:46:14.198123',
            '2018-11-21 12:46:14.756123',
            4,
            '2018-11-21 15:46:18.423123',
            'DEFAULT',
            'FAILED');
    launch4 = (SELECT currval(pg_get_serial_sequence('launch', 'id')));

    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.12.3', null, launch1, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('level', '1', null, launch1, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '3', null, launch1, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.9.1', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('level', '1', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('level', '2', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', 'passed', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.2.5', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('level', '2', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('level', '3', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.1.7.15.3', null, launch1, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.2.3', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '2', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '3.2.4.3', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', 'skipped', null, launch1, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.2.3', null, launch4, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', 'failed', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.3.2', null, launch2, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '1.9.1', null, launch3, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', '3', null, launch4, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES ('build', 'true_system_attr', null, launch1, true);


    INSERT INTO public.ticket (id, ticket_id, submitter, submit_date, bts_url, bts_project, url)
    VALUES (1, 'EPMRPP-322', 'superadmin', '2018-09-28 12:38:24.374555', 'jira.com', 'project', 'epam.com');
    INSERT INTO public.ticket (id, ticket_id, submitter, submit_date, bts_url, bts_project, url)
    VALUES (2, 'EPMRPP-123', 'superadmin', '2018-09-28 12:38:24.374555', 'jira.com', 'project', 'epam.com');
    INSERT INTO public.ticket (id, ticket_id, submitter, submit_date, bts_url, bts_project, url)
    VALUES (3, 'QWERTY-100', 'superadmin', '2018-09-28 12:38:24.374555', 'jira.com', 'project', 'epam.com');

    INSERT INTO public.pattern_template (id, name, "value", type, enabled, project_id)
    VALUES (1, 'FIRST PATTERN', 'aaaa', 'STRING', true, 1);
    INSERT INTO public.pattern_template (id, name, "value", type, enabled, project_id)
    VALUES (2, 'SECOND PATTERN', 'bbbb', 'STRING', true, 1);

    INSERT INTO test_item (test_case_hash, NAME, uuid, type, start_time, description, last_modified, unique_id, launch_id)
    VALUES (1, 'Step', 'uuid1', 'STEP', now(), 'description', now(), 'uniqueId', launch1);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35 + itemId, now());
    INSERT INTO public.pattern_template_test_item (pattern_id, item_id) VALUES (1, itemId);
    INSERT INTO public.pattern_template_test_item (pattern_id, item_id) VALUES (2, itemId);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);

    INSERT INTO test_item (test_case_hash, NAME, uuid, type, start_time, description, last_modified, unique_id, launch_id)
    VALUES (2, 'Step','uuid2', 'STEP', now(), 'description', now(), 'uniqueId', launch1);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35 + itemId, now());
    INSERT INTO issue (issue_id, issue_type, issue_description) VALUES (itemId, floor(random() * 5 + 1), 'issue description');
    INSERT INTO issue_ticket (issue_id, ticket_id) VALUES (itemId, 3);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);

    INSERT INTO test_item (test_case_hash, NAME, uuid, type, start_time, description, last_modified,
                           unique_id, launch_id)
    VALUES (3, 'Step', 'uuid3', 'STEP', now(), 'description', now(), 'uniqueId', launch1);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35 + itemId, now());
    INSERT INTO issue (issue_id, issue_type, issue_description) VALUES (itemId, floor(random() * 5 + 1), 'issue description');
    INSERT INTO issue_ticket (issue_id, ticket_id) VALUES (itemId, 2);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);

    INSERT INTO test_item (test_case_hash, NAME, uuid, type, start_time, description, last_modified, unique_id, launch_id)
    VALUES (4, 'Step','uuid4', 'STEP', now(), 'description', now(), 'uniqueId', launch1);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35 + itemId, now());
    INSERT INTO issue (issue_id, issue_type, issue_description) VALUES (itemId, floor(random() * 5 + 1), 'issue description');
    INSERT INTO issue_ticket (issue_id, ticket_id) VALUES (itemId, 1);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);

    INSERT INTO test_item (test_case_hash, NAME,uuid, type, start_time, description, last_modified, unique_id, launch_id)
    VALUES (5, 'Step','uuid5', 'STEP', now(), 'description', now(), 'uniqueId', launch4);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35, now());
    INSERT INTO issue (issue_id, issue_type, issue_description) VALUES (itemId, floor(random() * 5 + 1), 'issue description');
    INSERT INTO issue_ticket (issue_id, ticket_id) VALUES (itemId, 2);
    INSERT INTO public.pattern_template_test_item (pattern_id, item_id) VALUES (1, itemId);
    INSERT INTO public.pattern_template_test_item (pattern_id, item_id) VALUES (2, itemId);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);

    INSERT INTO test_item (test_case_hash, NAME, uuid, type, start_time, description, last_modified, unique_id, launch_id)
    VALUES (6, 'Step','uuid6','STEP', now(), 'description', now(), 'uniqueId', launch4);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35, now());
    INSERT INTO issue (issue_id, issue_type, issue_description) VALUES (itemId, floor(random() * 5 + 1), 'issue description');
    INSERT INTO issue_ticket (issue_id, ticket_id) VALUES (itemId, 1);
    INSERT INTO public.pattern_template_test_item (pattern_id, item_id) VALUES (2, itemId);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'lol', itemId, null, false);

    INSERT INTO test_item (test_case_hash, NAME, uuid, type, start_time, description, last_modified, unique_id, launch_id)
    VALUES (7, 'Step', 'uuid7', 'STEP', now(), 'description', now(), 'uniqueId', launch4);
    itemId = (SELECT (currval(pg_get_serial_sequence('test_item', 'item_id'))));
    INSERT INTO test_item_results (result_id, status, duration, end_time) VALUES (itemId, 'FAILED', 0.35, now());
    INSERT INTO issue (issue_id, issue_type, issue_description) VALUES (itemId, floor(random() * 5 + 1), 'issue description');
    INSERT INTO issue_ticket (issue_id, ticket_id) VALUES (itemId, 1);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'test', itemId, null, false);
    INSERT INTO item_attribute ("key", "value", item_id, launch_id, system) VALUES (null, 'value', itemId, null, false);

    ALTER SEQUENCE statistics_s_id_seq RESTART WITH 1;
    DELETE FROM statistics CASCADE;
    ALTER SEQUENCE statistics_field_sf_id_seq RESTART WITH 1;
    DELETE FROM statistics_field CASCADE;

    INSERT INTO statistics_field (sf_id, name) VALUES (1, 'statistics$executions$total');
    INSERT INTO statistics_field (sf_id, name) VALUES (2, 'statistics$executions$passed');
    INSERT INTO statistics_field (sf_id, name) VALUES (3, 'statistics$executions$skipped');
    INSERT INTO statistics_field (sf_id, name) VALUES (4, 'statistics$executions$failed');
    INSERT INTO statistics_field (sf_id, name) VALUES (5, 'statistics$defects$to_investigate$total');
    INSERT INTO statistics_field (sf_id, name) VALUES (6, 'statistics$defects$system_issue$total');
    INSERT INTO statistics_field (sf_id, name) VALUES (7, 'statistics$defects$automation_bug$total');
    INSERT INTO statistics_field (sf_id, name) VALUES (8, 'statistics$defects$product_bug$total');
    INSERT INTO statistics_field (sf_id, name) VALUES (9, 'statistics$defects$no_defect$total');
    INSERT INTO statistics_field (sf_id, name) VALUES (10, 'statistics$defects$to_investigate$ti001');
    INSERT INTO statistics_field (sf_id, name) VALUES (11, 'statistics$defects$system_issue$si001');
    INSERT INTO statistics_field (sf_id, name) VALUES (12, 'statistics$defects$automation_bug$ab001');
    INSERT INTO statistics_field (sf_id, name) VALUES (13, 'statistics$defects$product_bug$pb001');
    INSERT INTO statistics_field (sf_id, name) VALUES (14, 'statistics$defects$no_defect$nd001');

    -------------------------------------------------------------------------------------------------------------------<
    -------------------------------------------------------------------------------------------------------------------<
    -- LAUNCHES STATISTICS
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 1, 10);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 2, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 3, 4);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 4, 3);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 5, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 6, 8);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 7, 7);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 8, 13);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 9, 2);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 10, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 11, 8);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 12, 7);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 13, 13);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch1, 14, 2);

    --

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 1, 11);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 2, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 3, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 4, 6);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 5, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 6, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 7, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 8, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 9, 2);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 10, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 11, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 12, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 13, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch2, 14, 2);

    --

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 1, 15);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 2, 5);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 3, 5);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 4, 5);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 5, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 6, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 7, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 8, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 9, 1);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 10, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 11, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 12, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 13, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch3, 14, 1);

    --

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 1, 12);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 2, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 3, 1);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 4, 8);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 5, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 6, 4);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 7, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 8, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 9, 6);

    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 10, 3);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 11, 4);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 12, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 13, 2);
    INSERT INTO statistics (launch_id, statistics_field_id, s_counter) VALUES (launch4, 14, 6);

    INSERT INTO activity (action, event_name,  created_at, details, object_id, object_name, object_type, priority,
    project_id, organization_id, subject_id, subject_name, subject_type)
    VALUES ('CREATE', 'createLaunch', now(), null, null, 'objectName', 'LAUNCH', 'MEDIUM', 1, 1, 1, 'superadmin', 'USER');
    INSERT INTO activity (action, event_name, created_at, details, object_id, object_name, object_type, priority,
    project_id, organization_id, subject_id, subject_name, subject_type)
    VALUES ('CREATE', 'createLaunch', now(), null, null, 'objectName', 'LAUNCH', 'MEDIUM', 1, 1, 1, 'superadmin', 'USER');
    INSERT INTO activity (action, event_name, created_at, details, object_id, object_name, object_type, priority,
    project_id, organization_id, subject_id, subject_name, subject_type)
    VALUES ('CREATE', 'createLaunch', now(), null, null, 'objectName', 'LAUNCH', 'MEDIUM', 1, 1, 1, 'superadmin', 'USER');
    INSERT INTO activity (action, event_name, created_at, details, object_id, object_name, object_type, priority,
    project_id, organization_id, subject_id, subject_name, subject_type)
    VALUES ('CREATE', 'createLaunch', now(), null, null, 'objectName', 'LAUNCH', 'MEDIUM', 1, 1, 1, 'superadmin', 'USER');

END;
$$
    LANGUAGE plpgsql;
