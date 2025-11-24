CREATE OR REPLACE FUNCTION test_project_init()
    RETURNS VOID AS
$$
DECLARE
    falcon       BIGINT;
    death_star   BIGINT;
    han_solo     BIGINT;
    chubaka      BIGINT;
    fake_chubaka BIGINT;
    org_id       BIGINT;
BEGIN

    alter sequence project_id_seq restart with 3;

    INSERT INTO organization (name, slug, organization_type)
        VALUES ('Test organization', 'test-organization', 'INTERNAL');
    org_id := (SELECT currval(pg_get_serial_sequence('organization', 'id')));


    INSERT INTO project (name, key, slug, created_at, organization_id)
        VALUES ('millennium_falcon', 'millennium_falcon', 'prj-slug', now(), org_id);
    falcon := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO project (name, key, slug, created_at, organization_id)
    VALUES ('death_star', 'death_star', 'prj-death-star', now(), org_id);
    death_star := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata, uuid, external_id)
    VALUES ('han_solo', '3531f6f9b0538fd347f4c95bd2af9d01', 'han_solo@domain.com', 'ADMINISTRATOR',
            'INTERNAL', 'Han Solo', FALSE,
            '{"metadata": {"last_login": "1551187023768"}}', gen_random_uuid(), 'external_id_1');
    han_solo := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role)
    VALUES (han_solo, falcon, 'EDITOR');

    INSERT INTO organization_user (user_id, organization_id, organization_role)
       VALUES (han_solo, org_id, (SELECT 'MANAGER'::public."organization_role_enum"));

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata, uuid)
    VALUES ('chubaka', '601c4731aeff3b84f76672ad024bb2a0', 'chybaka@domain.com', 'USER', 'INTERNAL',
            'Chubaka', FALSE,
            '{"metadata": {"last_login": "1551187023768"}}', gen_random_uuid());
    chubaka := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (chubaka, falcon, 'VIEWER');

    INSERT INTO organization_user (user_id, organization_id, organization_role)
        VALUES (chubaka, org_id, (SELECT 'MEMBER'::public."organization_role_enum"));


    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata, uuid)
    VALUES ('fake_chubaka', '601c4731aeff3b84f76672ad024bb2a0', 'chybakafake@domain.com', 'USER',
            'INTERNAL', 'Chubaka Fake', FALSE,
            '{"metadata": {"last_login": "1551187023768"}}', gen_random_uuid());
    fake_chubaka := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO organization_user (user_id, organization_id, organization_role)
        VALUES (fake_chubaka, org_id, (SELECT 'MEMBER'::public."organization_role_enum"));

    INSERT INTO project_user (user_id, project_id, project_role)
    VALUES (fake_chubaka, falcon, 'VIEWER');

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata, uuid)
    VALUES ('ch_not_assigned', '601c4731aeff3b84f76672ad024bb2a0', 'ch_not_assigned@domain.com',
            'USER', 'INTERNAL', 'Ch Not Assigned', FALSE,
            '{"metadata": {"last_login": "1551187023768"}}', gen_random_uuid());

    INSERT INTO issue_type_project (project_id, issue_type_id)
    VALUES (falcon, 1),
           (falcon, 2),
           (falcon, 3),
           (falcon, 4),
           (falcon, 5);

    INSERT INTO project_attribute (project_id, attribute_id, value) VALUES (falcon, 1, '1 hour');
    INSERT INTO project_attribute (project_id, attribute_id, value) VALUES (falcon, 2, '2 weeks');
    INSERT INTO project_attribute (project_id, attribute_id, value) VALUES (falcon, 3, '2 weeks');
    INSERT INTO project_attribute (project_id, attribute_id, value) VALUES (falcon, 4, '2 weeks');

END
$$
    LANGUAGE plpgsql;
