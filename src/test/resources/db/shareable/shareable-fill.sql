INSERT INTO public.users (id, login, password, email, attachment, attachment_thumbnail, role, type, expired, full_name, metadata)
VALUES (3, 'jaja_user', '7c381f9d81b0e438af4e7094c6cae203', 'jaja@mail.com', null, null, 'USER', 'INTERNAL', false, 'Jaja Juja', '{"metadata": {"last_login": 1546605767372}}');

INSERT INTO public.project_user (user_id, project_id, project_role) VALUES (3, 1, 'MEMBER');

INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES
(1, false, 'superadmin', 1),
(2, true, 'superadmin', 1),
(3, false, 'default', 2),
(4, true, 'default', 2),
(5, true, 'superadmin', 1),
(6, false, 'superadmin', 1),
(7, true, 'superadmin', 1),
(8, true, 'jaja_user', 1),
(9, false, 'jaja_user', 1),
(10, false, 'default', 2),
(11, false, 'default', 2),
(12, true, 'default', 2),
(13, true, 'superadmin', 1),
(14, false, 'superadmin', 1),
(15, true, 'jaja_user', 1),
(16, false, 'jaja_user', 1),
(17, true, 'default', 2),
(18, false, 'default', 2);

INSERT INTO public.filter (id, name, target, description) VALUES
(1, 'Admin Filter', 'Launch', null),
(2, 'Admin Shared Filter', 'Launch', null),
(3, 'Default Filter', 'Launch', null),
(4, 'Default Shared Filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES
(1, 1, 'name', 'ASC'),
(2, 2, 'name', 'DESC'),
(3, 3, 'name', 'ASC'),
(4, 4, 'name', 'DESC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES
(1, 1, 'CONTAINS', 'asdf', 'name', false),
(2, 2, 'EQUALS', 'test', 'description', false),
(3, 3, 'EQUALS', 'juja', 'name', false),
(4, 4, 'EQUALS', 'qwerty', 'name', false);

INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options) VALUES
(5, 'activity stream12', null, 'activityStream', 50, '{"options": {"user": "superadmin", "actionType": ["startLaunch", "finishLaunch"]}}'),
(7, 'INVESTIGATED PERCENTAGE OF LAUNCHES', null, 'investigatedTrend', 10, '{"options": {"timeline": "DAY"}}'),
(6, 'LAUNCH STATISTICS', null, 'launchStatistics', 10, '{"options": {"timeline": "WEEK"}}'),
(8, 'TEST CASES GROWTH TREND CHART', null, 'casesTrend', 10, '{"options": {}}'),
(9, 'LAUNCHES DURATION CHART', null, 'launchesDurationChart', 10, '{"options": {}}'),
(12, 'ACTIVITY STREAM', null, 'activityStream', 10, '{"options": {"user": "default", "actionType": ["startLaunch", "finishLaunch", "deleteLaunch"]}}'),
(10, 'FAILED CASES TREND CHART', null, 'bugTrend', 10, '{"options": {}}'),
(11, 'LAUNCH STATISTICS', null, 'launchStatistics', 10, '{"options": {"timeline": "WEEK"}}');

insert into content_field(id, field)
values (10, 'statistics$executions$failed');

INSERT INTO public.widget_filter (widget_id, filter_id) VALUES
(6, 1),
(7, 2),
(8, 2),
(10, 3),
(11, 3);

INSERT INTO public.dashboard (id, name, description, creation_date) VALUES
(13, 'test admin dashboard', 'admin shared dashboard', '2019-01-10 13:01:06.083000'),
(14, 'test admin private dashboard', 'admin dashboard', '2019-01-10 13:01:19.259000'),
(15, 'test jaja shared dashboard', 'jaja dashboard', '2019-01-10 13:01:51.417000'),
(16, 'test jaja private dashboard', 'jaja dashboard', '2019-01-10 13:01:59.015000'),
(17, 'test default shared dashboard', 'default dashboard', '2019-01-10 13:02:20.397000'),
(18, 'test default private dashboard', 'default dashboard', '2019-01-10 13:02:27.659000');

INSERT INTO public.dashboard_widget (dashboard_id, widget_id, widget_name, widget_owner, widget_type, widget_width, widget_height,
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

INSERT INTO public.acl_sid (id, principal, sid) VALUES
(1, true, 'superadmin'),
(2, true, 'jaja_user'),
(3, true, 'default');

INSERT INTO public.acl_class (id, class, class_id_type) VALUES
(1, 'com.epam.ta.reportportal.entity.filter.UserFilter', 'java.lang.Long'),
(2, 'com.epam.ta.reportportal.entity.widget.Widget', 'java.lang.Long'),
(3, 'com.epam.ta.reportportal.entity.dashboard.Dashboard', 'java.lang.Long');

INSERT INTO public.acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) VALUES
(1, 1, '1', null, 1, true),
(2, 1, '2', null, 1, true),
(3, 1, '3', null, 3, true),
(4, 1, '4', null, 3, true),
(5, 2, '5', null, 1, true),
(6, 2, '6', null, 1, true),
(7, 2, '7', null, 1, true),
(8, 2, '8', null, 2, true),
(9, 2, '9', null, 2, true),
(10, 2, '10', null, 3, true),
(11, 2, '11', null, 3, true),
(12, 2, '12', null, 3, true),
(13, 3, '13', null, 1, true),
(14, 3, '14', null, 1, true),
(15, 3, '15', null, 2, true),
(16, 3, '16', null, 2, true),
(17, 3, '17', null, 3, true),
(18, 3, '18', null, 3, true);

INSERT INTO public.acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
(1, 1, 0, 1, 16, true, false, false),
(3, 2, 0, 2, 1, true, false, false),
(4, 2, 1, 1, 16, true, false, false),
(5, 3, 0, 3, 16, true, false, false),
(6, 4, 0, 3, 16, true, false, false),
(8, 5, 0, 2, 1, true, false, false),
(9, 5, 1, 1, 16, true, false, false),
(10, 6, 0, 1, 16, true, false, false),
(12, 7, 0, 2, 1, true, false, false),
(13, 7, 1, 1, 16, true, false, false),
(15, 8, 0, 1, 1, true, false, false),
(16, 8, 1, 2, 16, true, false, false),
(17, 9, 0, 2, 16, true, false, false),
(18, 10, 0, 3, 16, true, false, false),
(19, 11, 0, 3, 16, true, false, false),
(20, 12, 0, 3, 16, true, false, false),
(22, 13, 0, 2, 1, true, false, false),
(23, 13, 1, 1, 16, true, false, false),
(24, 14, 0, 1, 16, true, false, false),
(26, 15, 0, 1, 1, true, false, false),
(27, 15, 1, 2, 16, true, false, false),
(28, 16, 0, 2, 16, true, false, false),
(29, 17, 0, 3, 16, true, false, false),
(30, 18, 0, 3, 16, true, false, false);

alter sequence shareable_entity_id_seq restart with 19;
alter sequence filter_condition_id_seq restart with 5;
alter sequence filter_sort_id_seq restart with 5;
alter sequence acl_sid_id_seq restart with 4;
alter sequence acl_class_id_seq restart with 2;
alter sequence acl_object_identity_id_seq restart with 19;
alter sequence acl_entry_id_seq restart with 31;