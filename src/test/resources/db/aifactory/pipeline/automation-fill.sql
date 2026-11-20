-- Fixture for Automate on top of pipeline-fill.sql: test cases 900 and 901 (project 1) get a default version
-- and stay READY; the project gets a complete GitLab CI integration (type 9001 seeded by V230).
insert into tms_test_case_version (id, test_case_id, "name", is_default, is_draft)
values (9000, 900, 'Default Version', true, false), (9001, 901, 'Default Version', true, false);

update tms_test_case set status = 'READY' where id in (900, 901);

insert into integration (id, project_id, type, enabled, creator, creation_date, params, name)
values (910, 1, 9001, true, 'superadmin', now(),
        '{"params": {"projectId": "EPM-RPP/rp-tests", "triggerToken": "gl-test-token", "url": "https://git.epam.com"}}',
        'automation-gitlab-ci');
