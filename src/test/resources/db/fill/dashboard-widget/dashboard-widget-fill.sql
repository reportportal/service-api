DELETE
FROM public.users
WHERE id = 3;

INSERT INTO public.users (id, login, password, email, attachment, attachment_thumbnail, role, type,
                          expired, full_name, metadata)
VALUES (3, 'jaja_user', '7c381f9d81b0e438af4e7094c6cae203', 'jaja@mail.com', null, null, 'USER',
        'INTERNAL', false, 'Jaja Juja', '{"metadata": {"last_login": 1546605767372}}');

INSERT INTO public.project_user (user_id, project_id, project_role)
VALUES (3, 1, 'EDITOR');

INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (5, 'superadmin', 1),
       (6, 'superadmin', 1),
       (13, 'superadmin', 1),
       (14, 'superadmin', 1),
       (15, 'jaja_user', 1),
       (16, 'jaja_user', 1),
       (17, 'default', 2),
       (18, 'default', 2);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (5, 'activity stream12', null, 'activityStream', 50,
        '{"options": {"user": "superadmin", "actionType": ["startLaunch", "finishLaunch"]}}'),
       (6, 'LAUNCH STATISTICS', null, 'launchStatistics', 10, '{"options": {"timeline": "WEEK"}}');

INSERT INTO public.dashboard (id, name, description, creation_date)
VALUES (13, 'test admin dashboard', 'admin shared dashboard', '2019-01-10 13:01:06.083000'),
       (14, 'test admin private dashboard', 'admin dashboard', '2019-01-10 13:01:19.259000'),
       (15, 'test jaja shared dashboard', 'jaja dashboard', '2019-01-10 13:01:51.417000');

INSERT INTO public.dashboard_widget (dashboard_id, widget_id, widget_name, widget_owner,
                                     widget_type, widget_width, widget_height,
                                     widget_position_x,
                                     widget_position_y)
VALUES (13, 5, 'activity stream12', 'superadmin', 'activityStream', 5, 5, 0, 0),
       (13, 6, 'INVESTIGATED PERCENTAGE OF LAUNCHES', 'superadmin', 'investigatedTrend', 6, 3, 0,0),
       (14, 5, 'activity stream12', 'superadmin', 'activityStream', 5, 5, 0, 0),
       (14, 6, 'LAUNCH STATISTICS', 'superadmin', 'launchStatistics', 4, 6, 0, 0),
       (15, 5, 'TEST CASES GROWTH TREND CHART', 'jaja_user', 'topTestCases', 7, 3, 0, 0);
