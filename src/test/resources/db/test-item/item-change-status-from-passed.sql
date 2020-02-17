-- Passed launch with 4 step items
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'PASSED', false);

insert into item_attribute(id, key, value, item_id, launch_id, system) values (1, 'skippedIssue', 'true', null, 1, true);

-- Test level item with 2 step items
insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (1, 1, 'uuid1', 'test item 1', 'TEST', now(), 'desc', 'uuid1', now(), '1', null, 1);
insert into test_item_results(result_id, status)
values (1, 'IN_PROGRESS');


insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (2, 2, 'uuid2', 'step item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1.2', 1, 1);
insert into test_item_results(result_id, status)
values (2, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 2;

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (3, 3, 'uuid3', 'step item 2', 'STEP', now(), 'desc', 'uuid3', now(), '1.3', 1, 1);
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
insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (4, 4, 'uuid4', 'test item 1', 'TEST', now(), 'desc', 'uuid4', now(), '4', null, 1);
insert into test_item_results(result_id, status)
values (4, 'IN_PROGRESS');

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (5, 5, 'uuid5', 'step item 3', 'STEP', now(), 'desc', 'uuid5', now(), '4.5', 4, 1);
insert into test_item_results(result_id, status)
values (5, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 5;

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (6, 6, 'uuid6', 'step item 4', 'STEP', now(), 'desc', 'uuid6', now(), '4.6', 4, 1);
insert into test_item_results(result_id, status)
values (6, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 6;

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (7, 7, 'uuid7', 'step item 7', 'STEP', now(), 'desc', 'uuid7', now(), '4.7', 4, 1);
insert into test_item_results(result_id, status)
values (7, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 7;

update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 4;


insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (2, 'l2_uuid', 1, 1, 'test launch without skipped issue', 'desc', now(), null, 1, now(), 'DEFAULT', 'PASSED', false);

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (8, 8, 'uuid_s_8', 'test suite 1', 'SUITE', now(), 'desc', 'uuid4', now(), '8', null, 2);
insert into test_item_results(result_id, status)
values (8, 'IN_PROGRESS');

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (9, 9, 'uuid_s_9', 'step item under suite', 'STEP', now(), 'desc', 'uuid7', now(), '8.9', 8, 2);
insert into test_item_results(result_id, status)
values (9, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 9;

update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 8;


insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (3, 'l3_uuid', 1, 1, 'test launch to finish', 'desc', now(), null, 1, now(), 'DEFAULT', 'PASSED', false);

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (10, 10, 'uuid_s_2_8', 'test suite 2_1', 'SUITE', now(), 'desc', 'uuid4', now(), '10', null, 3);
insert into test_item_results(result_id, status)
values (10, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 10;

insert into test_item(test_case_hash, item_id, uuid,name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (11, 11, 'uuid_s_2_9', 'step item under suite 2_1', 'STEP', now(), 'desc', 'uuid7', now(), '10.11', 10, 3);
insert into test_item_results(result_id, status)
values (11, 'IN_PROGRESS');