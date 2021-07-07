insert into issue_type (id, issue_group_id, locator, issue_name, abbreviation, hex_color)
values (6, 1, 'custom_ti', 'Custom to investigate', 'CTI', '#2f39bf'),
       (7, 2, 'custom_ab', 'Custom automation bug', 'CAB', '#ccac39'),
       (8, 5, 'custom si', 'Custom system issue', 'CSI', '#08af2a');

insert into issue_type_project(project_id, issue_type_id)
values (2, 6),
       (2, 7),
       (2, 8);

INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, false, 'default', 2),
       (2, false, 'default', 2),
       (3, false, 'default', 2);


INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (2, 'overall statistics', null, 'overallStatistics', 20, '{"options": {}}'),
       (3, 'launches table', null, 'launchesTable', 20, '{"options": {}}');

insert into content_field(id, field)
values (2, 'statistics$executions$total'),
       (2, 'statistics$executions$passed'),
       (2, 'statistics$executions$failed'),
       (2, 'statistics$executions$skipped'),
       (2, 'statistics$executions$untested'),
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
       (3, 'statistics$executions$untested'),
       (3, 'statistics$defects$product_bug$pb001'),
       (3, 'statistics$defects$automation_bug$ab001'),
       (3, 'statistics$defects$to_investigate$custom_ti');


insert into widget_filter(widget_id, filter_id)
values (2, 1),
       (3, 1);

alter sequence issue_type_id_seq restart with 9;

insert into pattern_template(id, name, "value", type, enabled, project_id)
values (1, 'some_name', 'value', 'STRING', true, 2),
       (2, 'simple_name', 'value', 'STRING', true, 2),
       (3, 'another_name', 'value', 'STRING', true, 1);
alter sequence pattern_template_id_seq restart with 4;