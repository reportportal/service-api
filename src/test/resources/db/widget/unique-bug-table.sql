-- First launch
INSERT INTO launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
VALUES (1, 'uuid', 1, 1, 'test launch', 'desc', now(), NULL, 1, now(), 'DEFAULT', 'FAILED', FALSE);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (1, 1, 'uuid1', 'test item 1', 'STEP', now(), 'desc', 'uuid1', now(), '1', NULL, 1);
INSERT INTO test_item_results(result_id, status)
VALUES (1, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'PASSED',
    end_time = now()
WHERE result_id = 1;

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (2, 2, 'uuid2', 'test item 2', 'STEP', now(), 'desc', 'uuid2', now(), '2', NULL, 1);
INSERT INTO test_item_results(result_id, status)
VALUES (2, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'FAILED',
    end_time = now()
WHERE result_id = 2;
INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (2, 2, 'automation bug', FALSE, TRUE);
INSERT INTO ticket(id, ticket_id, submitter, bts_url, submit_date, url, bts_project)
VALUES (1, 'ticket1', 'superadmin', 'http:/example.com', now(), 'http:/example.com/ticket1', 'superadmin_bts');
INSERT INTO issue_ticket(issue_id, ticket_id)
VALUES (2, 1);
INSERT INTO item_attribute(key, value, item_id, launch_id, system)
VALUES (NULL, 'test', 2, NULL, FALSE),
       (NULL, 'value', 2, NULL, FALSE);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (3, 3, 'uuid3', 'test item 3', 'STEP', now(), 'desc', 'uuid3', now(), '3', NULL, 1);
INSERT INTO test_item_results(result_id, status)
VALUES (3, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'FAILED',
    end_time = now()
WHERE result_id = 3;
INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (3, 3, 'product bug', FALSE, TRUE);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (4, 4, 'uuid4', 'test item 4', 'STEP', now(), 'desc', 'uuid4', now(), '4', NULL, 1);
INSERT INTO test_item_results(result_id, status)
VALUES (4, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'PASSED',
    end_time = now()
WHERE result_id = 4;

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (5, 5, 'uuid5', 'test item 5', 'STEP', now(), 'desc', 'uuid5', now(), '5', NULL, 1);
INSERT INTO test_item_results(result_id, status)
VALUES (5, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'FAILED',
    end_time = now()
WHERE result_id = 5;
INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (5, 1, 'to investigate', FALSE, TRUE);

-- Second launch
INSERT INTO launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
VALUES (2, 'uuid2', 1, 1, 'test launch', 'desc', now(), NULL, 2, now(), 'DEFAULT', 'FAILED', FALSE);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (6, 6, 'uuid6', 'test item 1', 'STEP', now(), 'desc', 'uuid6', now(), '6', NULL, 2);
INSERT INTO test_item_results(result_id, status)
VALUES (6, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'PASSED',
    end_time = now()
WHERE result_id = 6;

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (7, 7, 'uuid7', 'test item 2', 'STEP', now(), 'desc', 'uuid7', now(), '7', NULL, 2);
INSERT INTO test_item_results(result_id, status)
VALUES (7, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'FAILED',
    end_time = now()
WHERE result_id = 7;
INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (7, 1, 'unknown bug', FALSE, TRUE);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (8, 8, 'uuid8', 'test item 3', 'STEP', now(), 'desc', 'uuid8', now(), '8', NULL, 2);
INSERT INTO test_item_results(result_id, status)
VALUES (8, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'FAILED',
    end_time = now()
WHERE result_id = 8;
INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (8, 3, 'product bug', FALSE, TRUE);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (9, 9, 'uuid9', 'test item 4', 'STEP', now(), 'desc', 'uuid9', now(), '9', NULL, 2);
INSERT INTO test_item_results(result_id, status)
VALUES (9, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'SKIPPED',
    end_time = now()
WHERE result_id = 9;

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, unique_id, last_modified, path, parent_id, launch_id)
VALUES (10, 10, 'uuid10', 'test item 5', 'STEP', now(), 'desc', 'uuid10', now(), '10', NULL, 2);
INSERT INTO test_item_results(result_id, status)
VALUES (10, 'IN_PROGRESS');
UPDATE test_item_results
SET status   = 'FAILED',
    end_time = now()
WHERE result_id = 10;
INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (10, 1, 'to investigate', FALSE, TRUE);

-- Filter and widget
INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (1, 'superadmin', 1),
       (2, 'superadmin', 1),
       (3, 'superadmin', 1),
       (4, 'superadmin', 1);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', NULL),
       (4, 'Not match any launch filter', 'Launch', NULL);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC'),
       (4, 4, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', FALSE),
       (4, 4, 'EQUALS', 'mot_exist', 'name', FALSE);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (2, 'unique bug table', NULL, 'uniqueBugTable', 10, '{
  "options": {}
}'),
       (3, 'unique bug table', NULL, 'uniqueBugTable', 10, '{
         "options": {}
       }');

INSERT INTO widget_filter(widget_id, filter_id)
VALUES (2, 1),
       (3, 4);