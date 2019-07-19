insert into log(id, uuid, log_time, log_message, item_id, last_modified, log_level)
values (1, 'uuid1', now(), 'log message', 2, now(), 40000),
       (2, 'uuid2', now(), 'message', 2, now(), 20000),
       (3, 'uuid3', now(), 'fatal', 2, now(), 50000);

alter sequence log_id_seq restart with 4;