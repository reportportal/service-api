insert into log(id, uuid, log_time, log_message, item_id, last_modified, log_level, project_id)
values (1, 'ddd29955-0b88-4843-9390-1315a53afcc1', now(), 'log message', 2, now(), 40000, 1),
       (2, '9ba98f41-2cde-4510-8503-d8eda901cc71', now(), 'message', 2, now(), 20000, 1),
       (3, '94894af2-f6bd-43d9-9fff-c6512d63eb1b', now(), 'fatal', 2, now(), 50000, 1);

alter sequence log_id_seq restart with 4;