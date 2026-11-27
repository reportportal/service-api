INSERT INTO public.organization (id, name, slug, organization_type)
VALUES (701, 'personal org', 'personal-org-701', 'PERSONAL'),
       (702, 'regular org', 'regular-org-702', 'INTERNAL');

INSERT INTO project (id, name, organization_id, key, slug, created_at)
VALUES (701, 'personal project', 701, 'personal-org-701.personal-project', 'personal-project-701', NOW()),
       (702, 'regular project', 702, 'regular-org-702.regular-project', 'regular-project-702', NOW());

INSERT INTO users (id, login, password, email, role, type, expired, full_name, metadata)
VALUES (701, 'personal_editor', 'x', 'personal_editor@domain.com', 'USER', 'INTERNAL', false, 'a', '{"metadata": {"last_login": "now"}}'),
       (702, 'personal_manager', 'x', 'personal_manager@domain.com', 'USER', 'INTERNAL', false, 'b', '{"metadata": {"last_login": "now"}}'),
       (703, 'regular_editor', 'x', 'regular_editor@domain.com', 'USER', 'INTERNAL', false, 'c', '{"metadata": {"last_login": "now"}}'),
       (704, 'regular_manager', 'x', 'regular_manager@domain.com', 'USER', 'INTERNAL', false, 'd', '{"metadata": {"last_login": "now"}}'),
       (705, 'both', 'x', 'both@domain.com', 'USER', 'INTERNAL', false, 'e', '{"metadata": {"last_login": "now"}}');

-- 701: EDITOR in a personal project only
INSERT INTO organization_user (user_id, organization_id, organization_role) VALUES (701, 701, 'MEMBER');
INSERT INTO project_user (user_id, project_id, project_role) VALUES (701, 701, 'EDITOR');
-- 702: MANAGER of a personal organization only
INSERT INTO organization_user (user_id, organization_id, organization_role) VALUES (702, 701, 'MANAGER');
-- 703: EDITOR in a project of a regular organization
INSERT INTO organization_user (user_id, organization_id, organization_role) VALUES (703, 702, 'MEMBER');
INSERT INTO project_user (user_id, project_id, project_role) VALUES (703, 702, 'EDITOR');
-- 704: MANAGER of a regular organization
INSERT INTO organization_user (user_id, organization_id, organization_role) VALUES (704, 702, 'MANAGER');
-- 705: roles in both
INSERT INTO organization_user (user_id, organization_id, organization_role) VALUES (705, 701, 'MANAGER');
INSERT INTO project_user (user_id, project_id, project_role) VALUES (705, 701, 'EDITOR');
INSERT INTO organization_user (user_id, organization_id, organization_role) VALUES (705, 702, 'MANAGER');
INSERT INTO project_user (user_id, project_id, project_role) VALUES (705, 702, 'EDITOR');
