INSERT INTO organization_settings (organization_id, setting_key, setting_value)
VALUES (101, 'retention_launches', '2592000')
ON CONFLICT DO NOTHING;

INSERT INTO organization_settings (organization_id, setting_key, setting_value)
VALUES (101, 'retention_logs', '2592000')
ON CONFLICT DO NOTHING;

INSERT INTO organization_settings (organization_id, setting_key, setting_value)
VALUES (101, 'retention_attachments', '2592000')
ON CONFLICT DO NOTHING;
