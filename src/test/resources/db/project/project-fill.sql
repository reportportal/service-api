insert into project (id, name, project_type, organization, creation_date)
values (3, 'test_project', 'INTERNAL', 'org', now());

insert into users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired, full_name, metadata)
values (3, 'test_user', '179AD45C6CE2CB97CF1029E212046E81', 'test@domain.com', null, null, 'USER', 'INTERNAL', false, 'test full name', '{"metadata": {"last_login": "now"}}');

insert into project_user(user_id, project_id, project_role) values (3, 3, 'MEMBER');
insert into project_user(user_id, project_id, project_role) values (1, 3, 'PROJECT_MANAGER');

insert into shareable_entity(id, shared, owner, project_id) values (1, true, 'superadmin', 3);
insert into filter(id, name, target, description) values (1, 'test filter', 'Launch', 'decription');
insert into filter_sort(filter_id, field) values (1, 'name');
insert into filter_condition(id, filter_id, condition, value, search_criteria, negative) values (1, 1, 'CONTAINS', 'asdf', 'name', false);

insert into user_preference(project_id, user_id, filter_id) values (3, 1, 1);

insert into shareable_entity(id, shared, owner, project_id) values (2, true, 'superadmin', 3);
insert into filter(id, name, target, description) values (2, 'test filter2', 'Launch', 'decription');
insert into filter_sort(filter_id, field) values (2, 'name');
insert into filter_condition(id, filter_id, condition, value, search_criteria, negative) values (2, 2, 'CONTAINS', 'kek', 'name', false);

alter sequence project_id_seq restart with 4;
alter sequence users_id_seq restart with 4;