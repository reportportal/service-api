INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
VALUES (1, 'name1', 'qwe', 'STRING', true, 1);
INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
VALUES (2, 'name2', 'qw', 'STRING', true, 1);
INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
VALUES (3, 'name3', 'qwee', 'STRING', false, 1);
INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
VALUES (4, 'name4', '[a-z]{2,4}', 'REGEX', false, 1);
INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
VALUES (5, 'name5_p2', '^*+', 'REGEX', true, 2);