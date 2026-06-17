ALTER TABLE sender_case
    ADD rule_type VARCHAR(55) NOT NULL DEFAULT 'email';

ALTER TABLE sender_case
    ADD rule_details JSONB NULL;

DROP INDEX unique_rule_name_per_project;

CREATE UNIQUE INDEX unique_rule_name_per_project_rule_type
    ON sender_case (rule_name, project_id, rule_type);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM attribute WHERE name = 'notifications.email.enabled') THEN
    PERFORM setval('attribute_id_seq', (SELECT MAX(id) FROM attribute));
        WITH new_attr AS (
            INSERT INTO attribute (name)
            VALUES ('notifications.email.enabled')
            RETURNING id
        )
        INSERT INTO project_attribute (attribute_id, value, project_id)
        SELECT new_attr.id, value, project_id
        FROM project_attribute, new_attr
        WHERE attribute_id = (SELECT id FROM attribute WHERE name = 'notifications.enabled');
    END IF;
END
$$;