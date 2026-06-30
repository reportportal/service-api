INSERT INTO attribute (id, name) VALUES (18, 'analyzer.uniqueError.enabled');
INSERT INTO project_attribute SELECT 18, 'true', id FROM project;
INSERT INTO attribute (id, name) VALUES (19, 'analyzer.uniqueError.removeNumbers');
INSERT INTO project_attribute SELECT 19, 'true', id FROM project;