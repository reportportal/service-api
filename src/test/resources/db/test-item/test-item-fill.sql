insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
values (1, 'a7b66ef2-db30-4db7-94df-f5f7786b398a', 2, 2, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'IN_PROGRESS');

insert into test_item(item_id, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (1, 'root item', 'SUITE', now(), 'desc', now(), '1', 'cf28e552-fe5a-4c80-a778-355a62f52efc', false, false, null, null, 1);

insert into test_item_results(result_id, status) values (1, 'IN_PROGRESS');

insert into item_attribute (key, value, item_id, launch_id, system)
values ('browser', 'chrome', 1, null, false),
       ('os', 'linux', 1, null, false);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
values (2, '45a80a5e-d73e-483a-a51f-43cc7f5111af', 2, 2, 'test launch 2', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED');

insert into test_item(item_id, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (2, 'child item', 'STEP', now(), 'desc', now(), '2', 'e1d24ec8-f321-499c-a56a-9d6afbd7f955', false, false, null, null, 2);

insert into test_item_results(result_id, status) values (2, 'PASSED');

insert into test_item(item_id, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (3, 'child item', 'STEP', now(), 'desc', now(), '3', '73737d72-6629-44b4-9047-3e7afb1936ba5', false, false, null, null, 2);

insert into test_item_results(result_id, status) values (3, 'FAILED');

insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer) values (3, 2, 'issue desc', false, true);

insert into bug_tracking_system (id, url, type, bts_project, project_id)
values (4, 'http://example.com', 'JIRA', 'test_project', 2);

insert into ticket(ticket_id, submitter_id, submit_date, bts_url, bts_project, url) values ('ticket', 2, now(), 'https://example.com', 'project', 'https://example.com/ticket');

alter sequence launch_id_seq restart with 3;
alter sequence test_item_item_id_seq restart with 4;