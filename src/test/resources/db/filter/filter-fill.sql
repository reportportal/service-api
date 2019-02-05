INSERT INTO public.users (id, login, password, email, attachment, attachment_thumbnail, role, type, expired, default_project_id, full_name, metadata) VALUES (3, 'jaja_user', '7c381f9d81b0e438af4e7094c6cae203', 'jaja@mail.com', null, null, 'USER', 'INTERNAL', false, null, 'Jaja Juja', '{"metadata": {"last_login": 1546605767372}}');

INSERT INTO public.project_user (user_id, project_id, project_role) VALUES (3, 1, 'MEMBER');

INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (1, false, 'superadmin', 1);
INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (2, true, 'superadmin', 1);
INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (3, false, 'default', 2);
INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (4, true, 'default', 2);

INSERT INTO public.filter (id, name, target, description) VALUES (1, 'Admin Filter', 'Launch', null);
INSERT INTO public.filter (id, name, target, description) VALUES (2, 'Admin shared Filter', 'Launch', null);
INSERT INTO public.filter (id, name, target, description) VALUES (3, 'Default Filter', 'Launch', null);
INSERT INTO public.filter (id, name, target, description) VALUES (4, 'Default shared Filter', 'Launch', null);

INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES (1, 1, 'name', 'ASC');
INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES (2, 2, 'name', 'ASC');
INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES (3, 3, 'name', 'DESC');
INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES (4, 4, 'name', 'ASC');

INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES (1, 1, 'EQUALS', 'DEFAULT', 'mode', false);
INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES (2, 2, 'CONTAINS', 'val', 'name', false);
INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES (3, 3, 'CONTAINS', 'def', 'name', false);
INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES (4, 4, 'CONTAINS', 'asdf', 'name', false);

INSERT INTO public.acl_sid (id, principal, sid) VALUES (1, true, 'superadmin');
INSERT INTO public.acl_sid (id, principal, sid) VALUES (2, true, 'jaja_user');
INSERT INTO public.acl_sid (id, principal, sid) VALUES (3, true, 'default');

INSERT INTO public.acl_class (id, class, class_id_type) VALUES (1, 'com.epam.ta.reportportal.entity.filter.UserFilter', 'java.lang.Long');

INSERT INTO public.acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) VALUES (1, 1, '1', null, 1, true);
INSERT INTO public.acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) VALUES (2, 1, '2', null, 1, true);
INSERT INTO public.acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) VALUES (3, 1, '3', null, 3, true);
INSERT INTO public.acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) VALUES (4, 1, '4', null, 3, true);

INSERT INTO public.acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (1, 1, 0, 1, 16, true, false, false);
INSERT INTO public.acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (3, 2, 0, 2, 1, true, false, false);
INSERT INTO public.acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (4, 2, 1, 1, 16, true, false, false);
INSERT INTO public.acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (5, 3, 0, 3, 16, true, false, false);
INSERT INTO public.acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES (6, 4, 0, 3, 16, true, false, false);

alter sequence shareable_entity_id_seq restart with 5;
alter sequence filter_condition_id_seq restart with 5;
alter sequence filter_sort_id_seq restart with 5;
alter sequence acl_sid_id_seq restart with 4;
alter sequence acl_class_id_seq restart with 2;
alter sequence acl_object_identity_id_seq restart with 5;
alter sequence acl_entry_id_seq restart with 7;