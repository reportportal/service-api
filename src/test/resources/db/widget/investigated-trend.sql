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

-- Second launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number,
                   last_modified, mode, status, has_retries)
values (2, 'uuid2', 1, 1, 'test launch', 'desc', now(), null, 2, now(), 'DEFAULT', 'FAILED', false);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (6, 6, 'uuid6', 'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '6', null, 2);
insert into test_item_results(result_id, status)
values (6, 'IN_PROGRESS');
update test_item_results
set status   = 'PASSED',
    end_time = now()
where result_id = 6;

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (7, 7, 'uuid7', 'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '7', null, 2);
insert into test_item_results(result_id, status)
values (7, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 7;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (7, 1, 'unknown bug', false, true);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (8, 8, 'uuid8', 'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '8', null, 2);
insert into test_item_results(result_id, status)
values (8, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 8;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (8, 3, 'product bug', false, true);

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (9, 9, 'uuid9', 'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '9', null, 2);
insert into test_item_results(result_id, status)
values (9, 'IN_PROGRESS');
update test_item_results
set status   = 'SKIPPED',
    end_time = now()
where result_id = 9;

insert into test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id,
                      last_modified, path, parent_id, launch_id)
values (10, 10, 'uuid10', 'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '10', null, 2);
insert into test_item_results(result_id, status)
values (10, 'IN_PROGRESS');
update test_item_results
set status   = 'FAILED',
    end_time = now()
where result_id = 10;
insert into issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
values (10, 1, 'to investigate', false, true);

-- Filter and widget
INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (1, 'admin@reportportal.internal', 1),
       (2, 'admin@reportportal.internal', 1),
       (3, 'admin@reportportal.internal', 1);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (2, 'investigated trend', null, 'investigatedTrend', 10, '{"options": {}}'),
       (3, 'investigated trend', null, 'investigatedTrend', 10,
        '{"options": {"timeline":  "WEEK"}}');

insert into widget_filter(widget_id, filter_id)
values (2, 1),
       (3, 1);

-- Statistics (1=total, 2=passed, 3=skipped, 4=failed, 5-6=automation_bug, 7-8=product_bug, 11-12=to_investigate)
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 1), (1, 2, 1);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 2), (1, 4, 2), (1, 5, 2), (1, 6, 2);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 3), (1, 4, 3), (1, 7, 3), (1, 8, 3);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 4), (1, 2, 4);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 5), (1, 4, 5), (1, 11, 5), (1, 12, 5);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 6), (1, 2, 6);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 7), (1, 4, 7), (1, 11, 7), (1, 12, 7);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 8), (1, 4, 8), (1, 7, 8), (1, 8, 8);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 9), (1, 3, 9);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 10), (1, 4, 10), (1, 11, 10), (1, 12, 10);
INSERT INTO statistics(s_counter, statistics_field_id, launch_id) VALUES (5, 1, 1), (2, 2, 1), (3, 4, 1), (1, 5, 1), (1, 6, 1), (1, 7, 1), (1, 8, 1), (1, 11, 1), (1, 12, 1);
INSERT INTO statistics(s_counter, statistics_field_id, launch_id) VALUES (5, 1, 2), (1, 2, 2), (3, 4, 2), (1, 3, 2), (2, 11, 2), (2, 12, 2), (1, 7, 2), (1, 8, 2);