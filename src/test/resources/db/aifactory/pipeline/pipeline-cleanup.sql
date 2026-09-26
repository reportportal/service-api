-- Only needed for tests that run with @Transactional(propagation = NOT_SUPPORTED) — see
-- PipelineIntegrationTest's retry tests. Those tests bypass the normal test-transaction
-- rollback (deliberately, so the retry service's own REQUIRES_NEW transactions can see the
-- fixture), so their rows must be deleted explicitly instead. Harmless no-op for every other
-- (rollback-based) test in the class, since the fixture rows are already gone by the time this
-- runs. `ON DELETE CASCADE` takes care of pipeline_iteration/pipeline_stage/pipeline_stage_test_case.
delete from pipeline where id in (900, 901, 902);
delete from integration where id = 900;
delete from tms_test_case where id in (900, 901);
delete from tms_test_folder where id = 900;
delete from tms_project_sequence where project_id = 1 and entity_type = 'TEST_CASE';
