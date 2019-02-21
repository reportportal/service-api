DO
$$DECLARE
  defaultProject BIGINT;
  superadminProject BIGINT;
  defaultId BIGINT;
  superadmin BIGINT;
  ldap BIGINT;
  rally BIGINT;
  jira BIGINT;
  email BIGINT;
BEGIN

    INSERT INTO server_settings (key, value) VALUES ('server.analytics.all', 'true');
    INSERT INTO server_settings (key, value) VALUES ('server.email.star_tls_enabled', 'false');
    INSERT INTO server_settings (key, value) VALUES ('server.email.password', null);
    INSERT INTO server_settings (key, value) VALUES ('server.email.port', '587');
    INSERT INTO server_settings (key, value) VALUES ('server.email.protocol', 'smtp');
    INSERT INTO server_settings (key, value) VALUES ('server.email.ssl_enabled', 'false');
    INSERT INTO server_settings (key, value) VALUES ('server.email.auth_enabled', 'false');
    INSERT INTO server_settings (key, value) VALUES ('server.email.enabled', 'true');
    INSERT INTO server_settings (key, value) VALUES ('server.email.username', null);
    INSERT INTO server_settings (key, value) VALUES ('server.email.host', null);
    INSERT INTO server_settings (key, value) VALUES ('server.analytics.asd', 'true');

    INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type) VALUES (true, 'test integration type', 'LDAP', now(), 'AUTH');
    ldap := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type) VALUES (true, 'RALLY', 'OAUTH', now(), 'BTS') ;
    rally := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type) VALUES (true, 'JIRA', 'BASIC', now(), 'BTS');
    jira := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (enabled, name, creation_date, group_type) VALUES (true, 'email', now(), 'NOTIFICATION');
    email := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (1, 'TO_INVESTIGATE');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (2, 'AUTOMATION_BUG');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (3, 'PRODUCT_BUG');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (4, 'NO_DEFECT');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (5, 'SYSTEM_ISSUE');

    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (1, 'ti001', 'To Investigate', 'TI', '#ffb743');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (2, 'ab001', 'Automation Bug', 'AB', '#f7d63e');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (3, 'pb001', 'Product Bug', 'PB', '#ec3900');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (4, 'nd001', 'No Defect', 'ND', '#777777');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (5, 'si001', 'System Issue', 'SI', '#0274d1');

    INSERT INTO attribute (name) VALUES ('job.interruptJobTime');
    INSERT INTO attribute (name) VALUES ('job.keepLaunches');
    INSERT INTO attribute (name) VALUES ('job.keepLogs');
    INSERT INTO attribute (name) VALUES ('job.keepScreenshots');
    INSERT INTO attribute (name) VALUES ('analyzer.minDocFreq');
    INSERT INTO attribute (name) VALUES ('analyzer.minTermFreq');
    INSERT INTO attribute (name) VALUES ('analyzer.minShouldMatch');
    INSERT INTO attribute (name) VALUES ('analyzer.numberOfLogLines');
    INSERT INTO attribute (name) VALUES ('analyzer.indexingRunning');
    INSERT INTO attribute (name) VALUES ('analyzer.isAutoAnalyzerEnabled');
    INSERT INTO attribute (name) VALUES ('analyzer.autoAnalyzerMode');
    INSERT INTO attribute (name) VALUES ('email.enabled');
    INSERT INTO attribute (name) VALUES ('email.from');

    -- Superadmin project and user
    INSERT INTO project (name, project_type, creation_date, metadata) VALUES ('superadmin_personal', 'PERSONAL', now(), '{"metadata": {"additional_info": ""}}');
    superadminProject := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata)
    VALUES ('superadmin', '5d39d85bddde885f6579f8121e11eba2', 'superadminemail@domain.com', 'ADMINISTRATOR', 'INTERNAL', 'tester', FALSE,
            '{"metadata": {"last_login": "now"}}');
    superadmin := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (superadmin, superadminProject, 'PROJECT_MANAGER');

    -- Default project and user
    INSERT INTO project (name, project_type, creation_date, metadata) VALUES ('default_personal', 'PERSONAL', now(), '{"metadata": {"additional_info": ""}}');
    defaultProject := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, full_name, expired, metadata)
    VALUES ('default', '3fde6bb0541387e4ebdadf7c2ff31123', 'defaultemail@domain.com', 'USER', 'INTERNAL', 'tester', FALSE,
            '{"metadata": {"last_login": "now"}}');
    defaultId := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (defaultId, defaultProject, 'PROJECT_MANAGER');

    -- Project configurations

    INSERT INTO issue_type_project (project_id, issue_type_id) VALUES
    (superadminProject, 1), (superadminProject, 2), (superadminProject, 3), (superadminProject, 4), (superadminProject, 5),
    (defaultProject, 1),(defaultProject, 2),(defaultProject, 3),(defaultProject, 4),(defaultProject, 5);

    INSERT INTO integration (project_id, type, enabled, creation_date) VALUES (superadminProject, rally, FALSE, now()), (defaultProject, rally, FALSE, now());
    INSERT INTO integration (project_id, type, enabled, creation_date) VALUES (superadminProject, jira, FALSE, now()), (defaultProject, jira, FALSE, now());

    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (1, '1 day', defaultProject), (1, '1 day', superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (2, '3 months', defaultProject), (2, '3 months', superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (3, '2 weeks', defaultProject), (3, '2 weeks', superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (4, '2 weeks', defaultProject), (4, '2 weeks', superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (5, 7, defaultProject), (5, 7, superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (6, 1, defaultProject), (6, 1, superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (7, 80, defaultProject), (7, 80, superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (8, 2, defaultProject), (8, 2, superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (9, false, defaultProject), (9, false, superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (10, false, defaultProject), (10, false, superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (11, 'LAUNCH_NAME', defaultProject), (11, 'LAUNCH_NAME', superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (12, 'true', defaultProject), (12, 'reportportal@example.com', superadminProject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (13, 'true', defaultProject), (13, 'reportportal@example.com', superadminProject);

    INSERT INTO integration (project_id, type, enabled, params)
      VALUES (defaultProject, email, false, '{"params": {"rules": [{"recipients": ["owner"]}, {"launchStatsRule": "always"}]}}');

    INSERT INTO integration (project_id, type, enabled, params)
      VALUES (superadminProject, email, false, '{"params": {"rules": [{"recipients": ["owner"]}, {"launchStatsRule": "always"}]}}');

END
$$;
