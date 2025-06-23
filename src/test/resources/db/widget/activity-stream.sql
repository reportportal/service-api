-- Activities
insert into activity(id, created_at, action, event_name, priority, object_id, object_name, object_type, subject_id,
subject_name, subject_type, project_id, organization_id)
values (1, now() + interval '1 milliseconds', 'START', 'startLaunch', 'LOW', 1, 'objectName', 'LAUNCH', 1, 'admin@reportportal.internal', 'USER', 1, 1),
       (2, now() + interval '2 milliseconds', 'UPDATE', 'updateItem', 'MEDIUM', 2, 'objectName', 'ITEM', 1, 'admin@reportportal.internal', 'USER', 1, 1),
       (3, now() + interval '3 milliseconds', 'DELETE', 'deleteLaunch', 'MEDIUM', 1, 'objectName', 'LAUNCH', 1, 'admin@reportportal.internal', 'USER', 1, 1),
       (4, now() + interval '4 milliseconds', 'DELETE', 'deleteLaunch', 'MEDIUM', 1, 'objectName', 'LAUNCH', 2, 'user', 'USER', 2, 1);

-- Filter and widget
INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (1, 'admin@reportportal.internal', 1),
       (2, 'admin@reportportal.internal', 1),
       (3, 'admin@reportportal.internal', 1),
       (4, 'admin@reportportal.internal', 1);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (1, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "admin@reportportal.internal", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}'),
       (2, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "admin@reportportal.internal", "actionType": ["updateLaunch"]}}'),
       (3, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "not_exist", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}'),
       (4, 'activity stream', null, 'activityStream', 20,
        '{"options": {"user":  "", "actionType": ["startLaunch", "updateItem", "deleteLaunch"]}}');
