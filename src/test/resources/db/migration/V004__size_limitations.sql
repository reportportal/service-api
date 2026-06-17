ALTER TABLE ticket
    ALTER COLUMN bts_url TYPE VARCHAR(1024),
    ALTER COLUMN bts_project TYPE VARCHAR(1024),
    ALTER COLUMN url TYPE VARCHAR(1024),
    ALTER COLUMN ticket_id TYPE VARCHAR(256);

ALTER TABLE test_item
    ALTER COLUMN unique_id TYPE VARCHAR(1024),
    ALTER COLUMN code_ref TYPE VARCHAR;

DROP INDEX IF EXISTS log_attach_id_idx;
CREATE INDEX IF NOT EXISTS log_attach_id_idx
    ON log (attachment_id);

DROP INDEX IF EXISTS log_launch_id_idx;
CREATE INDEX IF NOT EXISTS log_launch_id_idx
    ON log (launch_id);

DROP INDEX IF EXISTS activity_creation_date_idx;
CREATE INDEX IF NOT EXISTS activity_creation_date_idx
    ON activity (creation_date);

DROP INDEX IF EXISTS activity_object_idx;
CREATE INDEX IF NOT EXISTS activity_object_idx
    ON activity (object_id);