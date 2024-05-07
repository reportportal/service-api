INSERT INTO public.users (id, login, password, email, attachment, attachment_thumbnail, role, type,
                          expired, full_name, metadata)
VALUES (3, 'jaja_user', '7c381f9d81b0e438af4e7094c6cae203', 'jaja@mail.com', null, null, 'USER',
        'INTERNAL', false, 'Jaja Juja', '{"metadata": {"last_login": 1546605767372}}');

INSERT INTO public.organization_user (user_id, organization_id, organization_role)
    VALUES (3, 1, 'MEMBER'::public."organization_role_enum");

INSERT INTO public.project_user (user_id, project_id, project_role)
VALUES (3, 1, 'EDITOR'::public."project_role_enum");

INSERT INTO public.owned_entity (id, owner, project_id)
VALUES (1, 'superadmin', 1),
       (2, 'superadmin', 1),
       (3, 'default', 2),
       (4, 'default', 2),
       (5, 'superadmin', 1),
       (6, 'superadmin', 1),
       (7, 'superadmin', 1),
       (8, 'jaja_user', 1),
       (9, 'jaja_user', 1),
       (10, 'default', 2),
       (11, 'default', 2),
       (12, 'default', 2),
       (13, 'superadmin', 1),
       (14, 'superadmin', 1),
       (15, 'jaja_user', 1),
       (16, 'jaja_user', 1),
       (17, 'default', 2),
       (18, 'default', 2);

INSERT INTO public.filter (id, name, target, description)
VALUES (1, 'Admin Filter', 'Launch', null),
       (2, 'Admin Shared Filter', 'Launch', null),
       (3, 'Default Filter', 'Launch', null),
       (4, 'Default Shared Filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction)
VALUES (1, 1, 'name', 'ASC'),
       (2, 2, 'name', 'DESC'),
       (3, 3, 'name', 'ASC'),
       (4, 4, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative)
VALUES (1, 1, 'CONTAINS', 'asdf', 'name', false),
       (2, 2, 'EQUALS', 'test', 'description', false),
       (3, 3, 'EQUALS', 'juja', 'name', false),
       (4, 4, 'EQUALS', 'qwerty', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options)
VALUES (5, 'activity stream12', null, 'activityStream', 50,
        '{"options": {"user": "superadmin", "actionType": ["startLaunch", "finishLaunch"]}}'),
       (7, 'INVESTIGATED PERCENTAGE OF LAUNCHES', null, 'investigatedTrend', 10,
        '{"options": {"timeline": "DAY"}}'),
       (6, 'LAUNCH STATISTICS', null, 'launchStatistics', 10, '{"options": {"timeline": "WEEK"}}'),
       (8, 'TEST CASES GROWTH TREND CHART', null, 'casesTrend', 10, '{"options": {}}'),
       (9, 'LAUNCHES DURATION CHART', null, 'launchesDurationChart', 10, '{"options": {}}'),
       (12, 'ACTIVITY STREAM', null, 'activityStream', 10,
        '{"options": {"user": "default", "actionType": ["startLaunch", "finishLaunch", "deleteLaunch"]}}'),
       (10, 'FAILED CASES TREND CHART', null, 'bugTrend', 10, '{"options": {}}'),
       (11, 'LAUNCH STATISTICS', null, 'launchStatistics', 10, '{"options": {"timeline": "WEEK"}}');

insert into content_field(id, field)
values (10, 'statistics$executions$failed');

INSERT INTO public.widget_filter (widget_id, filter_id)
VALUES (6, 1),
       (7, 2),
       (8, 2),
       (10, 3),
       (11, 3);

INSERT INTO public.dashboard (id, name, description, creation_date)
VALUES (13, 'test admin dashboard', 'admin shared dashboard', '2019-01-10 13:01:06.083000'),
       (14, 'test admin private dashboard', 'admin dashboard', '2019-01-10 13:01:19.259000'),
       (15, 'test jaja shared dashboard', 'jaja dashboard', '2019-01-10 13:01:51.417000'),
       (16, 'test jaja private dashboard', 'jaja dashboard', '2019-01-10 13:01:59.015000'),
       (17, 'test default shared dashboard', 'default dashboard', '2019-01-10 13:02:20.397000'),
       (18, 'test default private dashboard', 'default dashboard', '2019-01-10 13:02:27.659000');

INSERT INTO public.dashboard_widget (dashboard_id, widget_id, widget_name, widget_owner,
                                     widget_type, widget_width, widget_height,
                                     widget_position_x,
                                     widget_position_y)
VALUES (13, 5, 'activity stream12', 'superadmin', 'activityStream', 5, 5, 0, 0),
       (13, 7, 'INVESTIGATED PERCENTAGE OF LAUNCHES', 'investigatedTrend', 'superadmin', 6, 3, 0, 0),
       (14, 5, 'activity stream12', 'superadmin', 'activityStream', 5, 5, 0, 0),
       (14, 6, 'LAUNCH STATISTICS', 'superadmin', 'launchStatistics', 4, 6, 0, 0),
       (15, 8, 'TEST CASES GROWTH TREND CHART', 'jaja_user', 'casesTrend', 7, 3, 0, 0),
       (16, 9, 'LAUNCHES DURATION CHART', 'jaja_user', 'launchesDurationChart', 3, 7, 0, 0),
       (17, 12, 'ACTIVITY STREAM', 'default', 'activityStream', 2, 8, 0, 0),
       (18, 10, 'FAILED CASES TREND CHART', 'default', 'bugTrend', 6, 5, 0, 0),
       (18, 11, 'LAUNCH STATISTICS', 'default', 'launchStatistics', 5, 5, 0, 0);

alter sequence shareable_entity_id_seq restart with 19;
alter sequence filter_condition_id_seq restart with 5;
alter sequence filter_sort_id_seq restart with 5;
