alter sequence launch_id_seq restart with 1;

INSERT INTO launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified, mode,
                   status)
VALUES (1, 'uuid-1', 2, 2, 'launch 1', now(), now(), now(), 'DEFAULT', 'FAILED');

INSERT INTO item_attribute(key, value, launch_id)
VALUES ('key1', 'value1', 1);
INSERT INTO item_attribute(key, value, launch_id)
VALUES ('key2', 'value2', 1);
INSERT INTO item_attribute(key, value, launch_id)
VALUES ('key3', 'value3', 1);
INSERT INTO item_attribute(key, value, launch_id)
VALUES ('key:semi', 'value:colon', 1);

INSERT INTO launch(id, uuid, project_id, user_id, name, start_time, end_time, last_modified, mode,
                   status)
VALUES (2, 'uuid-2', 2, 2, 'launch 2', now(), now(), now(), 'DEFAULT', 'FAILED');

INSERT INTO item_attribute(key, value, launch_id)
VALUES ('key1', 'value1', 2);
INSERT INTO item_attribute(key, value, launch_id)
VALUES ('key2', 'value2', 2);
