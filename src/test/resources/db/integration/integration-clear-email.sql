DELETE FROM integration
WHERE type = (SELECT id FROM integration_type WHERE name = 'email');
