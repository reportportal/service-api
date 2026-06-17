ALTER TABLE integration_type
ADD COLUMN plugin_type VARCHAR(128) DEFAULT 'EXTENSION' NOT NULL;

UPDATE integration_type
SET plugin_type = 'BUILT_IN'
WHERE name IN ('ldap', 'ad', 'saml', 'email');