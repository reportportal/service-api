-- First launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (2, 'uuid2', 1, 1, 'test launch', 'desc', now(), null, 2, now(), 'DEFAULT', 'FAILED', false);

insert into item_attribute(key, value, launch_id) VALUES ('key', 'value', 2);
insert into item_attribute(key, value, launch_id) VALUES ('key1', 'value1', 2);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (6, 6,'uuid6',  'test item 1', 'STEP', now(), 'desc', 'uuid6', now(), '6', null, 2);
insert into test_item_results(result_id, status)
values (6, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 6;

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (7, 7,'uuid7',  'test item 2', 'STEP', now(), 'desc', 'uuid7', now(), '7', null, 2);
insert into test_item_results(result_id, status)
values (7, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 7;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (7, 1, 'unknown bug', false, true);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (8, 8,'uuid8',  'test item 3', 'STEP', now(), 'desc', 'uuid8', now(), '8', null, 2);
insert into test_item_results(result_id, status)
values (8, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 8;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (8, 3, 'product bug', false, true);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (9, 9,'uuid9',  'test item 4', 'STEP', now(), 'desc', 'uuid9', now(), '9', null, 2);
insert into test_item_results(result_id, status)
values (9, 'IN_PROGRESS');
update test_item_results
set status   = 'SKIPPED',
    end_time = now()
where result_id = 9;

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (10, 10,'uuid10',  'test item 5', 'STEP', now(), 'desc', 'uuid10', now(), '10', null, 2);
insert into test_item_results(result_id, status)
values (10, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 10;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (10, 1, 'to investigate', false, true);

-- Filters and widgets
INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, false, 'superadmin', 1),
       (2, false, 'superadmin', 1),
       (3, false, 'superadmin', 1),
       (4, false, 'superadmin', 1);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', null),
       (4, 'Not match any launch filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC'),
       (4, 4, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', false),
       (4, 4, 'EQUALS', 'mot_exist', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (2, 'launches table', null, 'launchesTable', 20, '{"options": {}}'),
       (3, 'launches table', null, 'launchesTable', 20, '{"options": {}}');

insert into content_field(id, field)
values (2, 'name'),
       (2, 'status'),
       (2, 'endTime'),
       (2, 'lastModified'),
       (2, 'number'),
       (2, 'description'),
       (2, 'user'),
       (2, 'attributes'),
       (2, 'statistics$executions$total'),
       (2, 'statistics$executions$passed'),
       (2, 'statistics$executions$failed'),
       (2, 'statistics$executions$skipped'),
       (2, 'statistics$defects$product_bug$pb001'),
       (2, 'statistics$defects$automation_bug$ab001'),
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
       (3, 'statistics$defects$automation_bug$ab001');

insert into widget_filter(widget_id, filter_id)
values (2, 1),
       (3, 4);