alter sequence launch_id_seq restart with 1;

INSERT INTO launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified, mode,
                   status, retention_policy)
VALUES (1, 'uuid-important', 2, 2, 'important launch', now(), now(), now(), 'DEFAULT', 'FAILED',
        'IMPORTANT');

INSERT INTO launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified, mode,
                   status, retention_policy)
VALUES (2, 'uuid-regular', 2, 2, 'regular launch', now(), now(), now(), 'DEFAULT', 'PASSED',
        'REGULAR');
