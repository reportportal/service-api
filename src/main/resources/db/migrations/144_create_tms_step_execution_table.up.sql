-- Create tms_step_execution table for tracking nested step executions

CREATE TABLE IF NOT EXISTS tms_step_execution (
  id BIGSERIAL PRIMARY KEY,
  test_case_execution_id BIGINT NOT NULL REFERENCES tms_test_case_execution(id) ON DELETE CASCADE,
  test_item_id BIGINT NOT NULL REFERENCES test_item(item_id) ON DELETE CASCADE,
  launch_id BIGINT NOT NULL REFERENCES launch(id) ON DELETE CASCADE,
  project_id BIGINT NOT NULL,
  tms_step_id BIGINT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes for performance
CREATE INDEX idx_tms_step_execution_test_case ON tms_step_execution(test_case_execution_id);
CREATE INDEX idx_tms_step_execution_test_item ON tms_step_execution(test_item_id);
CREATE INDEX idx_tms_step_execution_launch ON tms_step_execution(launch_id);
CREATE INDEX idx_tms_step_execution_tms_step ON tms_step_execution(tms_step_id);

-- Add comments
COMMENT ON TABLE tms_step_execution IS 'Tracks executions of nested steps in manual scenarios';
COMMENT ON COLUMN tms_step_execution.test_case_execution_id IS 'Reference to parent test case execution';
COMMENT ON COLUMN tms_step_execution.test_item_id IS 'Reference to nested step test item (hasStats=false)';
COMMENT ON COLUMN tms_step_execution.tms_step_id IS 'Reference to original TMS step ID for correlation (non-FK)';
COMMENT ON COLUMN tms_step_execution.created_at IS 'Creation timestamp';
COMMENT ON COLUMN tms_step_execution.updated_at IS 'Last modification timestamp';
