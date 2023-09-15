-- Activities
insert into activity(id, created_at, action, event_name, priority, object_id, object_name, object_type, subject_id,
subject_name, subject_type, project_id)
values (1, now(), 'START', 'startLaunch', 'LOW', 1, 'objectName', 'LAUNCH', 1, 'superadmin', 'USER', 1),
       (2, now(), 'UPDATE', 'updateItem', 'MEDIUM', 2, 'objectName', 'ITEM', 1, 'superadmin', 'USER', 1),
       (3, now(), 'DELETE', 'deleteLaunch', 'MEDIUM', 1, 'objectName', 'LAUNCH', 1, 'superadmin', 'USER', 1),
       (4, now(), 'DELETE', 'deleteLaunch', 'MEDIUM', 1, 'objectName', 'LAUNCH', 2, 'user', 'USER', 2);

-- Filter and widget
INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (1, 'superadmin', 1),
       (2, 'superadmin', 1),
       (3, 'superadmin', 1),
       (4, 'superadmin', 1);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (1, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "superadmin", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}'),
       (2, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "superadmin", "actionType": ["updateLaunch"]}}'),
       (3, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "not_exist", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}'),
       (4, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}');