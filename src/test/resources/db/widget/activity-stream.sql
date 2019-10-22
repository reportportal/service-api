-- Activities
insert into activity(id, user_id, project_id, entity, action, creation_date, object_id)
values (1, 1, 1, 'LAUNCH', 'startLaunch', now(), 1),
       (2, 1, 1, 'ITEM', 'updateItem', now(), 2),
       (3, 1, 1, 'LAUNCH', 'deleteLaunch', now(), 1),
       (4, 2, 2, 'LAUNCH', 'deleteLaunch', now(), 1);

-- Filter and widget
INSERT INTO public.shareable_entity (id, shared, owner, project_id)
VALUES (1, false, 'superadmin', 1),
       (2, false, 'superadmin', 1),
       (3, false, 'superadmin', 1),
       (4, false, 'superadmin', 1);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (1, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "superadmin", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}'),
       (2, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "superadmin", "actionType": ["updateLaunch"]}}'),
       (3, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "not_exist", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}'),
       (4, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}');