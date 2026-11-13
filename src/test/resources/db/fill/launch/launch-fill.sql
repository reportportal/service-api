alter sequence launch_id_seq restart with 1;
alter sequence test_item_item_id_seq restart with 1;

SELECT launches_init();

INSERT INTO public.launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified,
                          mode, status)
VALUES (100, 'uuid', 2, 2, 'finished launch', now(), now(), now(), 'DEFAULT', 'FAILED');

INSERT INTO public.test_item(test_case_hash, item_id, uuid, type, start_time, last_modified,
                             has_children, has_retries, parent_id, launch_id)
VALUES (1, 1, 'uuid1', 'STEP', now(), now(), false, false, null, 100);

INSERT INTO public.test_item_results(result_id, status, end_time, duration)
VALUES (1, 'PASSED', now(), 1);

INSERT INTO public.test_item(test_case_hash, item_id, uuid, type, start_time, last_modified,
                             has_children, has_retries, parent_id, launch_id)
VALUES (2, 2, 'uuid2', 'STEP', now(), now(), false, true, null, 100);

INSERT INTO public.test_item_results(result_id, status, end_time, duration)
VALUES (2, 'FAILED', now(), 1);

INSERT INTO public.issue(issue_id, issue_type, issue_description)
VALUES (2, 1, 'fail');

INSERT INTO public.log(uuid, log_time, log_message, item_id, launch_id, last_modified, log_level,
                       attachment_id, project_id)
VALUES ('log uid', now(), 'message', 2, null, now(), 40000, null, 2);


INSERT INTO public.launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified,
                          mode, status)
VALUES (200, 'uuid3', 2, 2, 'finished launch', now(), now(), now(), 'DEFAULT', 'FAILED');

INSERT INTO public.test_item(test_case_hash, item_id, uuid, type, start_time, last_modified,
                             has_children, has_retries, parent_id, launch_id)
VALUES (3, 3, 'uuid4', 'STEP', now(), now(), false, false, null, 200);

INSERT INTO public.test_item_results(result_id, status, end_time, duration)
VALUES (3, 'IN_PROGRESS', now(), 1);

INSERT INTO public.log(uuid, log_time, log_message, item_id, launch_id, last_modified, log_level,
                       attachment_id, project_id)
VALUES ('log uid', now(), 'message', 3, null, now(), 40000, null, 2);

INSERT INTO public.launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified,
                          mode, status, retention_policy)
VALUES (300, 'uuid5', 2, 2, 'finished launch', now(), now(), now(), 'DEFAULT', 'FAILED', 'IMPORTANT');