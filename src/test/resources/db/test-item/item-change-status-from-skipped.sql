-- Passed launch with 4 step items
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED', false);

insert into item_attribute(id, key, value, item_id, launch_id, system) values (1, 'skippedIssue', 'true', null, 1, true);

-- Test level item with 2 step items
insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (1, 'test item 1', 'TEST', now(), 'desc', 'uuid1', now(), '1', null, 1);
insert into test_item_results(result_id, status)
values (1, 'IN_PROGRESS');


insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (2, 'step item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1.2', 1, 1);
insert into test_item_results(result_id, status)
values (2, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 2;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (3, 'step item 2', 'STEP', now(), 'desc', 'uuid3', now(), '1.3', 1, 1);
insert into test_item_results(result_id, status)
values (3, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 3;

update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 1;

-- Test level item with 2 step items
insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (4, 'test item 1', 'TEST', now(), 'desc', 'uuid4', now(), '4', null, 1);
insert into test_item_results(result_id, status)
values (4, 'IN_PROGRESS');

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (5, 'step item 3', 'STEP', now(), 'desc', 'uuid5', now(), '4.5', 4, 1);
insert into test_item_results(result_id, status)
values (5, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 5;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (6, 'step item 4', 'STEP', now(), 'desc', 'uuid6', now(), '4.6', 4, 1);
insert into test_item_results(result_id, status)
values (6, 'IN_PROGRESS');
update test_item_results
set status   = 'SKIPPED',
    end_time = now()
where result_id = 6;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (7, 'step item 7', 'STEP', now(), 'desc', 'uuid7', now(), '4.7', 4, 1);
insert into test_item_results(result_id, status)
values (7, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 7;

update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 4;