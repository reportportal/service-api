ALTER TABLE sender_case
    ADD rule_name VARCHAR(55);

UPDATE sender_case
SET rule_name = 'Rule' || id;

CREATE UNIQUE INDEX unique_rule_name_per_project
    ON sender_case (rule_name, project_id);

ALTER TABLE sender_case
    ALTER COLUMN rule_name SET NOT NULL;
