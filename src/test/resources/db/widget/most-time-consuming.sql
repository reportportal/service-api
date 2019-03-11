-- First launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED', false);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (1, 'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1', null, 1);
insert into test_item_results(result_id, status)
values (1, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now() + interval '165 second',
    duration = 165.0
where result_id = 1;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (2, 'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '2', null, 1);
insert into test_item_results(result_id, status)
values (2, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now() + interval '192 second',
    duration = 192.0
where result_id = 2;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (2, 2, 'automation bug', false, true);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (3, 'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '3', null, 1);
insert into test_item_results(result_id, status)
values (3, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now() + interval '337 second',
    duration = 337.0
where result_id = 3;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (3, 3, 'product bug', false, true);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (4, 'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '4', null, 1);
insert into test_item_results(result_id, status)
values (4, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now() + interval '87 second',
    duration = 87.0
where result_id = 4;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (5, 'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '5', null, 1);
insert into test_item_results(result_id, status)
values (5, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now() + interval '251 second',
    duration = 251.0
where result_id = 5;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (5, 1, 'to investigate', false, true);

-- Second launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (2, 'uuid1', 1, 1, 'empty launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED', false);

-- Filter and widgets
INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, false, 'superadmin', 1),
       (2, false, 'superadmin', 1),
       (3, false, 'superadmin', 1),
       (4, false, 'superadmin', 1),
       (5, false, 'superadmin', 1),
       (6, false, 'superadmin', 1);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', null),
       (2, 'Not match any launch filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC'),
       (2, 2, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', false),
       (2, 2, 'EQUALS', 'mot_exist', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (3, 'most time consuming', null, 'mostTimeConsuming', 20, '{"options": {"launchNameFilter": "test launch"}}'),
       (4, 'most time consuming', null, 'mostTimeConsuming', 20, '{"options": {"launchNameFilter": "empty launch"}}'),
       (5, 'most time consuming', null, 'mostTimeConsuming', 20, '{"options": {"launchNameFilter": "not exist"}}'),
       (6, 'most time consuming', null, 'mostTimeConsuming', 20,
        '{"options": {"launchNameFilter": "test launch", "includeMethods": true}}');

insert into widget_filter(widget_id, filter_id)
values (3, 1);