insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
values (1, 'a7b66ef2-db30-4db7-94df-f5f7786b398a', 2, 2, 'test launch', 'desc', now(), null, 1, now(), 'DEFAULT', 'IN_PROGRESS');

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (1, 1, '0f7ca5bc-cfae-4cc1-9682-e59c2860131e', 'root item', 'SUITE', now(), 'desc', now(), '1', 'cf28e552-fe5a-4c80-a778-355a62f52efc', true, false, null, null, 1);

insert into test_item_results(result_id, status) values (1, 'IN_PROGRESS');

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (6, 6, '253ea6e6-0f04-4c8c-801c-6776b9f626de', 'child item', 'STEP', now(), 'desc', now(), '1.6', '74c35be0-3ffc-4c72-adfd-28bb3671f210', false, false, 1, null, 1);

insert into item_attribute (key, value, item_id, launch_id, system)
values ('testKey', 'testValue', 6, null, false);

insert into test_item_results(result_id, status) values (6, 'FAILED');


insert into item_attribute (key, value, item_id, launch_id, system)
values ('browser', 'chrome', 1, null, false),
       ('os', 'linux', 1, null, false),
       ('testKey', 'testValue', 1, null, false);

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
values (2, '45a80a5e-d73e-483a-a51f-43cc7f5111af', 2, 2, 'test launch 2', 'desc', now(), null, 1, now(), 'DEFAULT', 'FAILED');

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (2, 2, 'f3960757-1a06-405e-9eb7-607c34683154', 'child item', 'STEP', now(), 'desc', now(), '2', 'e1d24ec8-f321-499c-a56a-9d6afbd7f955', false, false, null, null, 2);

insert into item_attribute (key, value, item_id, launch_id, system)
values ('testKey', 'testValue', 2, null, false);

insert into test_item_results(result_id, status) values (2, 'PASSED');

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (3, 3, '0b4e8847-03fc-4326-bbc3-2dca9c31e22d', 'child item', 'STEP', now(), 'desc', now(), '3', '73737d72-6629-44b4-9047-3e7afb1936ba5', false, false, null, null, 2);

insert into item_attribute (key, value, item_id, launch_id, system)
values ('testKey', 'testValue', 3, null, false);

insert into test_item_results(result_id, status) values (3, 'FAILED');

insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer) values (3, 2, 'issue desc', false, true);

insert into ticket(ticket_id, submitter, submit_date, bts_url, bts_project, url) values ('ticket', 'default', now(), 'https://example.com', 'project', 'https://example.com/ticket');

insert into launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status)
values (3, '334d153c-8f9c-4dff-8627-47dd003bee0f', 1, 1, 'test launch admin', 'desc', now(), null, 1, now(), 'DEFAULT', 'IN_PROGRESS');

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (4, 4, '53e165eb-4a51-4247-8e8c-debd865c2477', 'child item', 'STEP', now(), 'desc', now(), '4', 'abf63661-c86c-42f8-95e8-be4b76f42bd2', false, false, null, null, 3);

insert into item_attribute (key, value, item_id, launch_id, system)
values ('testKey', 'testValue', 4, null, false);

insert into test_item_results(result_id, status) values (4, 'PASSED');

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (5, 5, '3ab067e5-537b-45ff-9605-843ab695c96a', 'child item', 'STEP', now(), 'desc', now(), '5', '757b376f-dfa0-40db-9373-d8092ab404a4', false, false, null, null, 3);

insert into item_attribute (key, value, item_id, launch_id, system)
values ('testKey', 'testValue', 5, null, false);

insert into test_item_results(result_id, status) values (5, 'IN_PROGRESS');


-- Retry item
insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children, has_retries, parent_id, retry_of, launch_id)
values (7, 7, '3ab067e5-537b-45ff-9605-retry', 'retry item', 'STEP', now(), 'desc', now(), '6.7', '757b376f-dfa0-40db-9373-retry', false, false, 6, 6, null);

insert into test_item_results(result_id, status) values (7, 'FAILED');

INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (1, FALSE, 'default', 2);
INSERT INTO public.filter (id, name, target, description) VALUES (1, 'Admin Filter', 'Launch', NULL);
INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES (1, 1, 'name', 'ASC');
INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES (1, 1, 'CONTAINS', 'test', 'name', FALSE);

alter sequence launch_id_seq restart with 4;
alter sequence test_item_item_id_seq restart with 8;