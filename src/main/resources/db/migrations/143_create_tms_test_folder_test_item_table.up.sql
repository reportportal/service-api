-- Create tms_test_folder_test_item junction table for many-to-many relationship

CREATE TABLE IF NOT EXISTS tms_test_folder_test_item (
  id BIGSERIAL PRIMARY KEY,
  test_folder_id BIGINT NOT NULL REFERENCES tms_test_folder(id) ON DELETE CASCADE,
  test_item_id BIGINT NOT NULL REFERENCES test_item(item_id) ON DELETE CASCADE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  UNIQUE(test_folder_id, test_item_id)
);

-- Create indexes for performance
CREATE INDEX idx_tms_test_folder_test_item_folder ON tms_test_folder_test_item(test_folder_id);
CREATE INDEX idx_tms_test_folder_test_item_item ON tms_test_folder_test_item(test_item_id);

-- Add comments
COMMENT ON TABLE tms_test_folder_test_item IS 'Junction table linking test folders to SUITE test items in manual launches';
COMMENT ON COLUMN tms_test_folder_test_item.test_folder_id IS 'Reference to test folder';
COMMENT ON COLUMN tms_test_folder_test_item.test_item_id IS 'Reference to SUITE type test item';
COMMENT ON COLUMN tms_test_folder_test_item.created_at IS 'Creation timestamp';
