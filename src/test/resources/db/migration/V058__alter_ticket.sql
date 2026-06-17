ALTER TABLE ticket
    ADD IF NOT EXISTS plugin_name VARCHAR(128);

UPDATE ticket t
SET plugin_name = it.name
FROM integration i
         JOIN integration_type it ON it.id = i.type
WHERE params -> 'params' ->> 'url' = t.bts_url;