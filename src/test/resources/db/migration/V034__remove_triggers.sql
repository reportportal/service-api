DROP TRIGGER IF EXISTS after_attachment_removed ON attachment;
DROP FUNCTION IF EXISTS decrease_allocated_size;

DROP TRIGGER IF EXISTS after_attachment_inserted ON attachment;
DROP FUNCTION IF EXISTS increment_allocated_size;