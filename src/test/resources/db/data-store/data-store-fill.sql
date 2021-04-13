INSERT INTO launch(id, uuid, project_id, user_id, name, description, start_time, end_time, last_modified, mode, status, has_retries,
                   rerun, approximate_duration)
VALUES (1, 'uuid', 1, 1, 'launch', 'launch', now(), now(), now(), 'DEFAULT', 'FAILED', FALSE, FALSE, 0);

INSERT INTO test_item(test_case_hash, item_id, uuid, name, type, start_time, description, last_modified, path, unique_id, has_children,
                      has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (1, 1, 'uuid', 'item', 'STEP', now(), 'desc', now(), '1', 'uniqueId', FALSE, FALSE, TRUE, NULL, NULL, 1);

INSERT INTO log(id, uuid, log_time, log_message, item_id, last_modified, log_level, project_id)
VALUES (1, 'uuid', now(), 'msg', 1, now(), 40000, 1);