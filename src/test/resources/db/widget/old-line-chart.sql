-- First launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number,
                   last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED', false);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (1, 1, 'uuid1', 'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1', null, 1);
insert into test_item_results(result_id, status)
values (1, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 1;

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (2, 2, 'uuid2', 'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '2', null, 1);
insert into test_item_results(result_id, status)
values (2, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 2;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (2, 2, 'automation bug', false, true);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (3, 3, 'uuid3', 'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '3', null, 1);
insert into test_item_results(result_id, status)
values (3, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 3;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (3, 3, 'product bug', false, true);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (4, 4, 'uuid4', 'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '4', null, 1);
insert into test_item_results(result_id, status)
values (4, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 4;

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (5, 5, 'uuid5', 'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '5', null, 1);
insert into test_item_results(result_id, status)
values (5, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 5;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (5, 1, 'to investigate', false, true);

-- Filter and widget
INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (1, 'admin@reportportal.internal', 1),
       (2, 'admin@reportportal.internal', 1),
       (3, 'admin@reportportal.internal', 1),
       (4, 'admin@reportportal.internal', 1),
       (5, 'admin@reportportal.internal', 1),
       (6, 'admin@reportportal.internal', 1),
       (7, 'admin@reportportal.internal', 1);

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
VALUES (2, 'old line chart', null, 'oldLineChart', 10, '{"options": {}}'),
       (3, 'old line chart', null, 'oldLineChart', 10, '{"options": {}}'),
       (5, 'old line chart', null, 'oldLineChart', 10, '{"options": {"timeline":  "WEEK"}}'),
       (6, 'old line chart', null, 'oldLineChart', 10, '{"options": {"timeline":  "WEEK"}}'),
       (7, 'old line chart', null, 'oldLineChart', 10, '{"options": {"timeline":  "notPresent"}}');

insert into content_field(id, field)
values (2, 'statistics$executions$total'),
       (2, 'statistics$executions$passed'),
       (2, 'statistics$executions$failed'),
       (2, 'statistics$executions$skipped'),
       (2, 'statistics$defects$product_bug$pb001'),
       (2, 'statistics$defects$automation_bug$ab001'),
       (2, 'statistics$defects$to_investigate$ti001'),
       (3, 'statistics$executions$total'),
       (3, 'statistics$executions$passed'),
       (3, 'statistics$executions$failed'),
       (5, 'statistics$executions$total'),
       (5, 'statistics$executions$passed'),
       (5, 'statistics$executions$failed'),
       (5, 'statistics$executions$skipped'),
       (5, 'statistics$defects$product_bug$pb001'),
       (5, 'statistics$defects$automation_bug$ab001'),
       (5, 'statistics$defects$to_investigate$ti001'),
       (6, 'statistics$executions$total'),
       (6, 'statistics$executions$passed'),
       (6, 'statistics$executions$failed'),
       (7, 'statistics$executions$total'),
       (7, 'statistics$executions$passed'),
       (7, 'statistics$executions$failed');

insert into widget_filter(widget_id, filter_id)
values (2, 1),
       (3, 4),
       (5, 1),
       (6, 4),
       (7, 4);