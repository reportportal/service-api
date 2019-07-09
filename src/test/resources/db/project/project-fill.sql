insert into project (id, name, project_type, organization, creation_date)
values (3, 'test_project', 'INTERNAL', 'org', now());

INSERT INTO project_attribute (attribute_id, value, project_id)
VALUES (1, '1 day', 3),
       (2, '3 months', 3),
       (3, '2 weeks', 3),
       (4, '2 weeks', 3),
       (5, 7, 3),
       (6, 1, 3),
       (7, 80, 3),
       (8, 2, 3),
       (9, FALSE, 3),
       (10, FALSE, 3),
       (11, 'LAUNCH_NAME', 3),
       (12, 'true', 3),
       (13, 'reportportal@example.com', 3);

insert into users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired, full_name, metadata)
values (3, 'test_user', '179AD45C6CE2CB97CF1029E212046E81', 'test@domain.com', null, null, 'USER', 'INTERNAL', false, 'test full name',
        '{"metadata": {"last_login": "now"}}');

insert into project_user(user_id, project_id, project_role)
values (3, 3, 'MEMBER');
insert into project_user(user_id, project_id, project_role)
values (1, 3, 'PROJECT_MANAGER');

insert into shareable_entity(id, shared, owner, project_id)
values (1, true, 'superadmin', 3);
insert into filter(id, name, target, description)
values (1, 'test filter', 'Launch', 'decription');
insert into filter_sort(filter_id, field)
values (1, 'name');
insert into filter_condition(id, filter_id, condition, value, search_criteria, negative)
values (1, 1, 'CONTAINS', 'asdf', 'name', false);

insert into user_preference(project_id, user_id, filter_id)
values (3, 1, 1);

insert into shareable_entity(id, shared, owner, project_id)
values (2, true, 'superadmin', 3);
insert into filter(id, name, target, description)
values (2, 'test filter2', 'Launch', 'decription');
insert into filter_sort(filter_id, field)
values (2, 'name');
insert into filter_condition(id, filter_id, condition, value, search_criteria, negative)
values (2, 2, 'CONTAINS', 'kek', 'name', false);

-- First launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now(), null, 3, now(), 'DEFAULT', 'FAILED', false);

insert into item_attribute(id, "key", "value", item_id, launch_id, system) values (1, 'key', 'val', null, 1, false);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (1, 'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1', null, 1);
insert into test_item_results(result_id, status)
values (1, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 1;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (2, 'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '2', null, 1);
insert into test_item_results(result_id, status)
values (2, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 2;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (2, 2, 'automation bug', false, true);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (3, 'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '3', null, 1);
insert into test_item_results(result_id, status)
values (3, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 3;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (3, 3, 'product bug', false, true);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (4, 'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '4', null, 1);
insert into test_item_results(result_id, status)
values (4, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 4;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (5, 'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '5', null, 1);
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
values (2, 'uuid2', 3, 1, 'test launch', 'desc', now(), null, 2, now(), 'DEFAULT', 'FAILED', false);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (6, 'test item 1', 'STEP', now(), 'desc', 'uuid6', now(), '6', null, 2);
insert into test_item_results(result_id, status)
values (6, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 6;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (7, 'test item 2', 'STEP', now(), 'desc', 'uuid7', now(), '7', null, 2);
insert into test_item_results(result_id, status)
values (7, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 7;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (7, 1, 'unknown bug', false, true);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (8, 'test item 3', 'STEP', now(), 'desc', 'uuid8', now(), '8', null, 2);
insert into test_item_results(result_id, status)
values (8, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 8;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (8, 3, 'product bug', false, true);

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (9, 'test item 4', 'STEP', now(), 'desc', 'uuid9', now(), '9', null, 2);
insert into test_item_results(result_id, status)
values (9, 'IN_PROGRESS');
update test_item_results
set status   = 'SKIPPED',
    end_time = now()
where result_id = 9;

insert into test_item(item_id, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
values (10, 'test item 5', 'STEP', now(), 'desc', 'uuid10', now(), '10', null, 2);
insert into test_item_results(result_id, status)
values (10, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 10;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (10, 1, 'to investigate', false, true);

-- Third launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (3, 'uuid3', 1, 1, 'empty launch', 'desc', now(), null, 2, now(), 'DEFAULT', 'FAILED', false);

alter sequence project_id_seq restart with 4;
alter sequence users_id_seq restart with 4;