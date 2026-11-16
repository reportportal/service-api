alter sequence sender_case_id_seq
    restart with 1;
INSERT INTO sender_case (id, rule_name, send_case, project_id, enabled, rule_type)
VALUES (1, 'Rule1', 'ALWAYS', 1, true, 'email');

INSERT INTO recipients(sender_case_id, recipient)
VALUES (1, 'first');
INSERT INTO recipients(sender_case_id, recipient)
VALUES (1, 'second');

INSERT INTO sender_case (id, rule_name, send_case, project_id, enabled, rule_type)
VALUES (2, 'Rule2', 'ALWAYS', 1, true, 'email');

INSERT INTO sender_case (id, rule_name, send_case, project_id, enabled, rule_type)
VALUES (3, 'Rule3', 'ALWAYS', 1, true, 'email');

INSERT INTO sender_case (id, rule_name, send_case, project_id, enabled, rule_type)
VALUES (4, 'Rule4', 'ALWAYS', 1, true, 'email');
