DO
$$DECLARE
  defaultproject BIGINT;
  superadminproject BIGINT;
  defaultid BIGINT;
  superadmin BIGINT;
  ldap BIGINT;
  email BIGINT;
BEGIN

    INSERT INTO server_settings (key, value) VALUES ('server.analytics.all', 'true');
    INSERT INTO server_settings (key, value) VALUES ('server.details.instance', gen_random_uuid());

    INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type) VALUES (TRUE, 'ldap', 'LDAP', now(), 'AUTH');
    ldap := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (enabled, name, creation_date, group_type) VALUES (TRUE, 'email', now(), 'NOTIFICATION');
    email := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (1, 'TO_INVESTIGATE'),
                                                                 (2, 'AUTOMATION_BUG'),
                                                                 (3, 'PRODUCT_BUG'),
                                                                 (4, 'NO_DEFECT'),
                                                                 (5, 'SYSTEM_ISSUE');

    ALTER SEQUENCE issue_group_issue_group_id_seq RESTART WITH 6;

    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (1, 'ti001', 'To Investigate', 'TI', '#ffb743');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (2, 'ab001', 'Automation Bug', 'AB', '#f7d63e');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (3, 'pb001', 'Product Bug', 'PB', '#ec3900');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (4, 'nd001', 'No Defect', 'ND', '#777777');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (5, 'si001', 'System Issue', 'SI', '#0274d1');

    ALTER SEQUENCE issue_type_id_seq RESTART WITH 6;

    INSERT INTO statistics_field (sf_id, name) VALUES (1, 'statistics$executions$total'),
                                                      (2, 'statistics$executions$passed'),
                                                      (3, 'statistics$executions$skipped'),
                                                      (4, 'statistics$executions$failed'),
                                                      (5, 'statistics$defects$automation_bug$total'),
                                                      (6, 'statistics$defects$automation_bug$ab001'),
                                                      (7, 'statistics$defects$product_bug$total'),
                                                      (8, 'statistics$defects$product_bug$pb001'),
                                                      (9, 'statistics$defects$system_issue$total'),
                                                      (10, 'statistics$defects$system_issue$si001'),
                                                      (11, 'statistics$defects$to_investigate$total'),
                                                      (12, 'statistics$defects$to_investigate$ti001'),
                                                      (13, 'statistics$defects$no_defect$total'),
                                                      (14, 'statistics$defects$no_defect$nd001');

    ALTER SEQUENCE statistics_field_sf_id_seq RESTART WITH 15;

    INSERT INTO attribute (name) VALUES ('job.interruptJobTime');
    INSERT INTO attribute (name) VALUES ('job.keepLaunches');
    INSERT INTO attribute (name) VALUES ('job.keepLogs');
    INSERT INTO attribute (name) VALUES ('job.keepScreenshots');
    INSERT INTO attribute (name) VALUES ('analyzer.minDocFreq');
    INSERT INTO attribute (name) VALUES ('analyzer.minTermFreq');
    INSERT INTO attribute (name) VALUES ('analyzer.minShouldMatch');
    INSERT INTO attribute (name) VALUES ('analyzer.numberOfLogLines');
    INSERT INTO attribute (name) VALUES ('analyzer.indexingRunning');
    INSERT INTO attribute (name) VALUES ('analyzer.isAutoPatternAnalyzerEnabled');
    INSERT INTO attribute (name) VALUES ('analyzer.isAutoAnalyzerEnabled');
    INSERT INTO attribute (name) VALUES ('analyzer.autoAnalyzerMode');
    INSERT INTO attribute (name) VALUES ('notifications.enabled');
    INSERT INTO attribute (name) VALUES ('email.from');


    -- Superadmin project and user
    INSERT INTO project (name, project_type, creation_date, metadata) VALUES ('superadmin_personal', 'PERSONAL', now(), '{"metadata": {"additional_info": ""}}');
    superadminproject := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata)
    VALUES ('superadmin', '5d39d85bddde885f6579f8121e11eba2', 'superadminemail@domain.com', 'ADMINISTRATOR', 'INTERNAL', 'tester', FALSE,
            '{"metadata": {"last_login": 0}}');
    superadmin := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (superadmin, superadminproject, 'PROJECT_MANAGER');

    -- Default project and user
    INSERT INTO project (name, project_type, creation_date, metadata) VALUES ('default_personal', 'PERSONAL', now(), '{"metadata": {"additional_info": ""}}');
    defaultproject := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata)
    VALUES ('default', '3fde6bb0541387e4ebdadf7c2ff31123', 'defaultemail@domain.com', 'USER', 'INTERNAL', 'tester', FALSE,
            '{"metadata": {"last_login": 0}}');
    defaultid := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (defaultid, defaultproject, 'PROJECT_MANAGER');

    -- Project configurations

    INSERT INTO issue_type_project (project_id, issue_type_id) VALUES
    (superadminproject, 1), (superadminproject, 2), (superadminproject, 3), (superadminproject, 4), (superadminproject, 5),
    (defaultproject, 1),(defaultproject, 2),(defaultproject, 3),(defaultproject, 4),(defaultproject, 5);


    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (1, '1 day', defaultproject), (1, '1 day', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (2, '3 months', defaultproject), (2, '3 months', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (3, '2 weeks', defaultproject), (3, '2 weeks', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (4, '2 weeks', defaultproject), (4, '2 weeks', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (5, 7, defaultproject), (5, 7, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (6, 1, defaultproject), (6, 1, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (7, 80, defaultproject), (7, 80, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (8, 2, defaultproject), (8, 2, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (9, FALSE, defaultproject), (9, FALSE, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (10, FALSE, defaultproject), (10, FALSE, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (11, FALSE, defaultproject), (11, FALSE, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (12, 'LAUNCH_NAME', defaultproject), (12, 'LAUNCH_NAME', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (13, 'false', defaultproject), (13, 'false', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (14, 'reportportal@example.com', defaultproject), (14, 'reportportal@example.com', superadminproject);

END
$$;
