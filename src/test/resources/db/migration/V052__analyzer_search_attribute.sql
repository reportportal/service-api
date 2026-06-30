INSERT INTO attribute (id, name) VALUES (17, 'analyzer.searchLogsMinShouldMatch');
INSERT INTO project_attribute SELECT 17, '95', id FROM project;