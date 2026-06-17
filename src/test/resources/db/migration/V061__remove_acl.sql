DROP TABLE IF EXISTS acl_entry;
DROP TABLE IF EXISTS acl_object_identity;
DROP TABLE IF EXISTS acl_class;
DROP INDEX IF EXISTS acl_sid_idx;
DROP TABLE IF EXISTS acl_sid;

ALTER TABLE shareable_entity DROP COLUMN shared;
ALTER TABLE dashboard_widget DROP COLUMN share;
ALTER TABLE shareable_entity RENAME TO owned_entity;

DROP TRIGGER IF EXISTS after_widget_update ON widget;
DROP FUNCTION IF EXISTS update_share_flag();