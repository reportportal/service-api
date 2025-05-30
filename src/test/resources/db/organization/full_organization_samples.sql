/*
 * Copyright 2024 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


----------------------------
--  Create users
----------------------------

-- admin
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (101, 'admin', '179AD45C6CE2CB97CF1029E212046E81', 'admin@example.com', NULL, NULL,
        'ADMINISTRATOR', 'INTERNAL', FALSE, 'Simply admin',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- no projects admin
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (102, 'no-projects-admin', '179AD45C6CE2CB97CF1029E212046E81',
        'no-projects-admin@example.com', NULL, NULL, 'ADMINISTRATOR', 'INTERNAL', FALSE,
        'no projects admin',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- no orgs admin
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (103, 'no-orgs-admin', '179AD45C6CE2CB97CF1029E212046E81', 'no-orgs-admin@example.com', NULL,
        NULL, 'ADMINISTRATOR', 'INTERNAL', FALSE, 'no orgs admin',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- user manager
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (104, 'user-manager', '179AD45C6CE2CB97CF1029E212046E81', 'user-manager@example.com', NULL,
        NULL, 'USER', 'INTERNAL', FALSE, 'user manager',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- user member editor
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (105, 'user-member-editor', '179AD45C6CE2CB97CF1029E212046E81',
        'user-member-editor@example.com', NULL, NULL, 'USER', 'INTERNAL', FALSE,
        'user member editor',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- user member viewer
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (106, 'user-member-viewer', '179AD45C6CE2CB97CF1029E212046E81',
        'user-member-viewer@example.com', NULL, NULL, 'USER', 'INTERNAL', FALSE,
        'user member viewer',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- no projects user
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (107, 'no-projects-user', '179AD45C6CE2CB97CF1029E212046E81', 'no-projects-user@example.com',
        NULL, NULL, 'USER', 'INTERNAL', FALSE, 'no projects user',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

-- no orgs user
INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (108, 'no-orgs-user', '179AD45C6CE2CB97CF1029E212046E81', 'no-orgs-user@example.com', NULL,
        NULL, 'USER', 'INTERNAL', FALSE, 'no orgs user',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

INSERT INTO users(id, login, password, email, attachment, attachment_thumbnail, role, type, expired,
                  full_name, metadata)
VALUES (109, 'upsa-user', '179AD45C6CE2CB97CF1029E212046E81', 'upsa-user@example.com', NULL,
        NULL, 'USER', 'UPSA', FALSE, 'upsa user',
        '{
          "metadata": {
            "last_login": "now"
          }
        }');

----------------------------
--  Create organizations
----------------------------
INSERT INTO public.organization (id, name, slug, organization_type)
VALUES (201, 'internal Org1', 'internal-org1', 'INTERNAL');

INSERT INTO public.organization (id, name, slug, organization_type)
VALUES (202, 'internal Org2', 'internal-org2', 'INTERNAL');

--no projects organization
INSERT INTO public.organization (id, name, slug, organization_type)
VALUES (203, 'internal no projects Org3', 'internal-no-projects-org3', 'INTERNAL');

-- external organization
INSERT INTO public.organization (id, name, slug, organization_type)
VALUES (204, 'UPSA Org4', 'upsa-org4', 'EXTERNAL');


----------------------------
--  Create projects
----------------------------
INSERT INTO project (id, name, organization_id, key, slug, created_at)
VALUES (301, 'test project 1', 201, 'internal-org1.test-project-1', 'test-project-1', NOW());

INSERT INTO project (id, name, organization_id, key, slug, created_at)
VALUES (302, 'test project 2', 202, 'internal-org2.test-project-2', 'test-project-2', NOW());

INSERT INTO project (id, name, organization_id, key, slug, created_at)
VALUES (303, 'test project 3', 202, 'internal-org2.test-project-3', 'test-project-3', NOW());

INSERT INTO project (id, name, organization_id, key, slug, created_at)
VALUES (304, 'test project 4', 204, 'upsa-org4.test-project-4', 'test-project-4', NOW());


-------------------------------
--  Assign organization roles
-------------------------------
-- assign admin
insert into organization_user (user_id, organization_id, organization_role)
values (101, 201, 'MANAGER');
insert into organization_user (user_id, organization_id, organization_role)
values (101, 202, 'MANAGER');

-- assign no-projects-admin
insert into organization_user (user_id, organization_id, organization_role)
values (102, 203, 'MANAGER');

-- assign user-manager
insert into organization_user (user_id, organization_id, organization_role)
values (104, 201, 'MANAGER');
insert into organization_user (user_id, organization_id, organization_role)
values (104, 202, 'MANAGER');
insert into organization_user (user_id, organization_id, organization_role)
values (104, 203, 'MANAGER');

-- assign user-member-editor
insert into organization_user (user_id, organization_id, organization_role)
values (105, 201, 'MEMBER');
insert into organization_user (user_id, organization_id, organization_role)
values (105, 202, 'MEMBER');

-- assign user-member-viewer
insert into organization_user (user_id, organization_id, organization_role)
values (106, 201, 'MEMBER');
insert into organization_user (user_id, organization_id, organization_role)
values (106, 202, 'MEMBER');

-- assign no-projects-user
insert into organization_user (user_id, organization_id, organization_role)
values (107, 201, 'MEMBER');
insert into organization_user (user_id, organization_id, organization_role)
values (107, 202, 'MEMBER');

