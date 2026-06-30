ALTER TABLE user_creation_bid
    ADD COLUMN project_name VARCHAR;

UPDATE user_creation_bid
SET project_name = (SELECT name FROM project WHERE project.id = user_creation_bid.default_project_id);

ALTER TABLE user_creation_bid
    ALTER COLUMN project_name SET NOT NULL;

ALTER TABLE user_creation_bid
    DROP COLUMN default_project_id;

ALTER TABLE user_creation_bid
    ADD COLUMN metadata JSONB;

UPDATE user_creation_bid
SET metadata = '{
  "metadata": {
    "type": "internal"
  }
}';
