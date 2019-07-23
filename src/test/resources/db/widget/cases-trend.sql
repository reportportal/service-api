-- First launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED', false);

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (1,'uuid1',  'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1', null, 1);
insert into test_item_results(result_id, status)
values (1, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 1;

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (2,'uuid2',  'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '2', null, 1);
insert into test_item_results(result_id, status)
values (2, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 2;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (2, 2, 'automation bug', false, true);

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (3,'uuid3',  'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '3', null, 1);
insert into test_item_results(result_id, status)
values (3, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 3;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (3, 3, 'product bug', false, true);

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (4,'uuid4',  'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '4', null, 1);
insert into test_item_results(result_id, status)
values (4, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 4;

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (5,'uuid5',  'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '5', null, 1);
insert into test_item_results(result_id, status)
values (5, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 5;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (5, 1, 'to investigate', false, true);

-- Second launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (2, 'uuid2', 1, 1, 'test launch', 'desc', now(), null, 2, now(), 'DEFAULT', 'FAILED', false);

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (6,'uuid6',  'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '6', null, 2);
insert into test_item_results(result_id, status)
values (6, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 6;

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (7, 'uuid7', 'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '7', null, 2);
insert into test_item_results(result_id, status)
values (7, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 7;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (7, 1, 'unknown bug', false, true);

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (8,'uuid8',  'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '8', null, 2);
insert into test_item_results(result_id, status)
values (8, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 8;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (8, 3, 'product bug', false, true);

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (9,'uuid9',  'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '9', null, 2);
insert into test_item_results(result_id, status)
values (9, 'IN_PROGRESS');
update test_item_results
set status   = 'SKIPPED',
    end_time = now()
where result_id = 9;

insert into test_item(item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (10,'uuid10',  'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '10', null, 2);
insert into test_item_results(result_id, status)
values (10, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 10;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (10, 1, 'to investigate', false, true);

-- Filter and widget
INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, false, 'superadmin', 1),
       (2, false, 'superadmin', 1),
       (3, false, 'superadmin', 1),
       (4, false, 'superadmin', 1),
       (5, false, 'superadmin', 1),
       (6, false, 'superadmin', 1),
       (7, false, 'superadmin', 1),
       (8, false, 'superadmin', 1);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', null),
       (2, 'Not match any launch filter', 'Launch', null),
       (3, 'Admin Filter Desc', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC'),
       (2, 2, 'name', 'DESC'),
       (3, 2, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', false),
       (2, 2, 'EQUALS', 'mot_exist', 'name', false),
       (3, 1, 'CONTAINS', 'test', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (4, 'cases trend', null, 'casesTrend', 10, '{"options": {}}'),
       (5, 'cases trend', null, 'casesTrend', 10, '{"options": {"timeline":  "WEEK"}}'),
       (6, 'cases trend', null, 'casesTrend', 10, '{"options": {}}'),
       (7, 'cases trend', null, 'casesTrend', 10, '{"options": {"timeline":  "notPresent"}}'),
       (8, 'cases trend', null, 'casesTrend', 10, '{"options": {"timeline":  "WEEK"}}');

insert into content_field(id, field)
values (4, 'statistics$executions$total'),
       (5, 'statistics$executions$total'),
       (6, 'statistics$executions$total'),
       (7, 'statistics$executions$total'),
       (8, 'statistics$executions$total');

insert into widget_filter(widget_id, filter_id)
values (4, 1),
       (5, 1),
       (6, 2),
       (7, 1),
       (8, 3);