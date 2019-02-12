insert into log(id, log_time, log_message, item_id, last_modified, log_level)
values (1, now(), 'log message', 2, now(), 40000),
       (2, now(), 'message', 2, now(), 20000),
       (3, now(), 'fatal', 2, now(), 50000);

alter sequence log_id_seq restart with 4;