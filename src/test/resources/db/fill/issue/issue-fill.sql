INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (5, trunc(random() * 5 + 1), 'description', false, false);

INSERT INTO issue(issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (106, trunc(random() * 5 + 1), 'description', false, false);

INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 1, 5), (1, 1, 106);
INSERT INTO statistics(s_counter, statistics_field_id, item_id) VALUES (1, 4, 5), (1, 4, 106);
