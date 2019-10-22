-- First launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (1, 'uuid', 1, 1, 'test launch', 'desc', now() - interval '9 minute', now(), 1, now(), 'DEFAULT', 'FAILED', false);

-- Second launch
insert into launch(id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status, has_retries)
values (2, 'uuid2', 1, 1, 'test launch', 'desc', now() - interval '11 minute', now(), 2, now(), 'DEFAULT', 'FAILED', false);

-- Filters and widgets
INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, false, 'superadmin', 1),
       (2, false, 'superadmin', 1),
       (3, false, 'superadmin', 1),
       (4, false, 'superadmin', 1);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', null),
       (4, 'Not match any launch filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC'),
       (4, 4, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'test', 'name', false),
       (4, 4, 'EQUALS', 'mot_exist', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (2, 'launches duration', null, 'launchesDurationChart', 20, '{"options": {}}'),
       (3, 'launches duration', null, 'launchesDurationChart', 20, '{"options": {}}');

insert into widget_filter(widget_id, filter_id)
values (2, 1),
       (3, 4);