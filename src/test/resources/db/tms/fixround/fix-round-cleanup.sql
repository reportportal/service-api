-- The fix round test commits (the Library announces a change once it is committed), so everything it created is
-- removed here. Deleting the pipeline cascades to its iterations, stages and stage/test-case links; deleting the
-- test cases cascades to their versions, scores, review comments and fix rounds.
delete from pipeline where project_id = 1 and name = 'Test case generation';
delete from tms_test_case where test_folder_id = 920;
delete from tms_test_folder where id = 920;
delete from tms_quality_standard where id = 920;
delete from integration where id = 920;
delete from tms_project_sequence where project_id = 1 and entity_type = 'TEST_CASE';
