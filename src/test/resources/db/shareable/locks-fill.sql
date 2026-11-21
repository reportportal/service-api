-- Extends shareable-fill.sql: a non-owner editor, an organization manager and a locked dashboard with locked widgets.
INSERT INTO public.users (id, login, password, email, attachment, attachment_thumbnail, role, type, expired, full_name, metadata)
VALUES (30, 'editor2@mail.com', '7c381f9d81b0e438af4e7094c6cae203', 'editor2@mail.com', null, null, 'USER',
        'INTERNAL', false, 'Second Editor', '{"metadata": {"last_login": 1546605767372}}'),
       (31, 'manager2@mail.com', '7c381f9d81b0e438af4e7094c6cae203', 'manager2@mail.com', null, null, 'USER',
        'INTERNAL', false, 'Org Manager', '{"metadata": {"last_login": 1546605767372}}');

INSERT INTO public.organization_user (user_id, organization_id, organization_role)
VALUES (30, 1, 'MEMBER'::public."organization_role_enum"),
       (31, 1, 'MANAGER'::public."organization_role_enum");

INSERT INTO public.project_user (user_id, project_id, project_role)
VALUES (30, 1, 'EDITOR'::public."project_role_enum"),
       (31, 1, 'VIEWER'::public."project_role_enum");

-- jaja owns dashboard 13 and its widgets 5 and 7; widget 5 is also placed on admin's unlocked dashboard 14
UPDATE public.owned_entity SET owner = 'jaja@mail.com' WHERE id IN (5, 7, 13);
UPDATE public.owned_entity SET locked = true WHERE id IN (13, 5, 7);
