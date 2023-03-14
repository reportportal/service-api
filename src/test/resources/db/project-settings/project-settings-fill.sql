INSERT INTO issue_type (id, issue_group_id, locator, issue_name, abbreviation, hex_color)
VALUES (6, 1, 'custom_ti', 'Custom to investigate', 'CTI', '#2f39bf'),
       (7, 2, 'custom_ab', 'Custom automation bug', 'CAB', '#ccac39'),
       (8, 5, 'custom si', 'Custom system issue', 'CSI', '#08af2a');

INSERT INTO issue_type_project(project_id, issue_type_id)
VALUES (2, 6),
       (2, 7),
       (2, 8);

INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, FALSE, 'default', 2),
       (2, FALSE, 'default', 2),
       (3, FALSE, 'default', 2);


INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'filter', 'Launch', NULL);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', FALSE);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (2, 'overall statistics', NULL, 'overallStatistics', 20, '{"options": {}}'),
       (3, 'launches table', NULL, 'launchesTable', 20, '{"options": {}}');

INSERT INTO content_field(id, field)
VALUES (2, 'statistics$executions$total'),
       (2, 'statistics$executions$passed'),
       (2, 'statistics$executions$failed'),
       (2, 'statistics$executions$skipped'),
       (2, 'statistics$defects$product_bug$pb001'),
       (2, 'statistics$defects$automation_bug$ab001'),
       (2, 'statistics$defects$to_investigate$ti001'),
       (3, 'name'),
       (3, 'status'),
       (3, 'endTime'),
       (3, 'lastModified'),
       (3, 'number'),
       (3, 'description'),
       (3, 'user'),
       (3, 'attributes'),
       (3, 'statistics$executions$total'),
       (3, 'statistics$executions$passed'),
       (3, 'statistics$executions$failed'),
       (3, 'statistics$executions$skipped'),
       (3, 'statistics$defects$product_bug$pb001'),
       (3, 'statistics$defects$automation_bug$ab001'),
       (3, 'statistics$defects$to_investigate$custom_ti');


INSERT INTO widget_filter(widget_id, filter_id)
VALUES (2, 1),
       (3, 1);

ALTER SEQUENCE issue_type_id_seq RESTART WITH 9;

INSERT INTO pattern_template(id, name, value, type, enabled, project_id)
VALUES (1, 'some_name', 'value', 'STRING', TRUE, 2),
       (2, 'simple_name', 'value', 'STRING', TRUE, 2),
       (3, 'another_name', 'value', 'STRING', TRUE, 1);
ALTER SEQUENCE pattern_template_id_seq RESTART WITH 4;

INSERT INTO launch(id, uuid, project_id, user_id, name, description, start_time, end_time,
                   last_modified, mode, status, has_retries,
                   rerun, approximate_duration)
VALUES (1, 'uuid', 2, 1, 'launch', 'launch', now(), now(), now(), 'DEFAULT', 'FAILED', FALSE, FALSE,
        0);

INSERT INTO public.sender_case (id, send_case, project_id, enabled, rule_name)
VALUES (1, 'ALWAYS', 2, TRUE, 'rule #1'),
       (2, 'FAILED', 2, FALSE, 'rule #2'),
       (3, 'TO_INVESTIGATE', 2, FALSE, 'rule #3'),
       (4, 'MORE_10', 2, TRUE, 'rule #4');

ALTER SEQUENCE sender_case_id_seq RESTART WITH 5;

INSERT INTO public.launch_names (sender_case_id, launch_name)
VALUES (1, 1);