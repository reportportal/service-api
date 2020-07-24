INSERT INTO users (login, password, email, role, type, full_name, expired, metadata)
VALUES ('default_customer', '5d39d85bddde885f6579f8121e11eba2', 'customeremail@domain.com', 'USER', 'INTERNAL', 'tester', FALSE,
        '{"metadata": {"last_login": 0}}');

INSERT INTO project_user (user_id, project_id, project_role) VALUES ((SELECT currval(pg_get_serial_sequence('users', 'id'))), 2, 'CUSTOMER');
