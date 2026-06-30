-- add allocated storage fields to all projects

ALTER TABLE project
    ADD COLUMN allocated_storage BIGINT NOT NULL DEFAULT 0;

UPDATE project AS prj
SET allocated_storage = (SELECT coalesce(sum(attachment.file_size), 0) FROM attachment WHERE project_id = prj.id);

-- increase allocated storage on insert

CREATE OR REPLACE FUNCTION increment_allocated_size()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE project SET allocated_storage = allocated_storage + new.file_size WHERE id = new.project_id;
    RETURN new;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER after_attachment_inserted
    AFTER INSERT
    ON attachment
    FOR EACH ROW
EXECUTE PROCEDURE increment_allocated_size();


-- decrease allocated storage on delete

CREATE OR REPLACE FUNCTION decrease_allocated_size()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE project
    SET allocated_storage = (CASE WHEN (allocated_storage - old.file_size < 0) THEN 0 ELSE allocated_storage - old.file_size END)
    WHERE id = old.project_id;
    RETURN old;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER after_attachment_removed
    AFTER DELETE
    ON attachment
    FOR EACH ROW
EXECUTE PROCEDURE decrease_allocated_size();