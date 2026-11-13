CREATE OR REPLACE FUNCTION test_group_init()
    RETURNS VOID AS
$$
DECLARE
    falcon       BIGINT;
    death_star   BIGINT;
    han_solo     BIGINT;
    chubaka      BIGINT;
    fake_chubaka BIGINT;
    rebel        BIGINT;
    ewoks        BIGINT;
    empire       BIGINT;
    jedi         BIGINT;
    sith_order   BIGINT;
BEGIN
    falcon := (SELECT p.id FROM project p WHERE name = 'millennium_falcon');
    death_star := (SELECT p.id FROM project p WHERE name = 'death_star');
    han_solo := (SELECT u.id FROM users u WHERE login = 'han_solo');
    chubaka := (SELECT u.id FROM users u WHERE login = 'chubaka');
    fake_chubaka := (SELECT u.id FROM users u WHERE login = 'fake_chubaka');

    INSERT INTO groups (name, slug, created_by, created_at, uuid, org_id)
    VALUES ('Rebel army', 'rebel-group', 1, now(), gen_random_uuid(), 1);
    rebel := (SELECT currval(pg_get_serial_sequence('groups', 'id')));

    INSERT INTO groups (name, slug, created_by, created_at, uuid, org_id)
    VALUES ('Ewoks group', 'ewoks-group', 1, now(), gen_random_uuid(), 1);
    ewoks := (SELECT currval(pg_get_serial_sequence('groups', 'id')));

    INSERT INTO groups (name, slug, created_by, created_at, uuid, org_id)
    VALUES ('Empire group', 'empire-group', 1, now(), gen_random_uuid(), 1);
    empire := (SELECT currval(pg_get_serial_sequence('groups', 'id')));

    INSERT INTO groups (name, slug, created_by, created_at, uuid, org_id)
    VALUES ('Jedi group', 'jedi-group', 1, now(), gen_random_uuid(), 1);
    jedi := (SELECT currval(pg_get_serial_sequence('groups', 'id')));

    INSERT INTO groups (name, slug, created_by, created_at, uuid, org_id)
    VALUES ('Sith order group', 'sith-group', 1, now(), gen_random_uuid(), 1);
    sith_order := (SELECT currval(pg_get_serial_sequence('groups', 'id')));

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (rebel, han_solo, now());

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (rebel, chubaka, now());

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (ewoks, chubaka, now());

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (ewoks, fake_chubaka, now());

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (empire, fake_chubaka, now());

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (jedi, fake_chubaka, now());

    INSERT INTO groups_users (group_id, user_id, created_at)
    VALUES (sith_order, fake_chubaka, now());

    INSERT INTO groups_projects (group_id, project_id, project_role, created_at)
    VALUES (rebel, falcon, 'EDITOR', now());

    INSERT INTO groups_projects (group_id, project_id, project_role, created_at)
    VALUES (ewoks, falcon, 'VIEWER', now());

    INSERT INTO groups_projects (group_id, project_id, project_role, created_at)
    VALUES (jedi, falcon, 'VIEWER', now());

    INSERT INTO groups_projects (group_id, project_id, project_role, created_at)
    VALUES (empire, death_star, 'EDITOR', now());

    INSERT INTO groups_projects (group_id, project_id, project_role, created_at)
    VALUES (sith_order, death_star, 'VIEWER', now());

END;
$$
    LANGUAGE plpgsql;
