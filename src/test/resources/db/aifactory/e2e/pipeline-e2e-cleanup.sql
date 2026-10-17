-- The e2e test commits (it has to: the Library announces a change once it is committed), so everything it
-- created is removed here. Deleting the pipeline cascades to its iterations, stages and stage/test-case links.
delete from pipeline where project_id = 1 and name = 'Test case generation';
delete from tms_test_case where test_folder_id = 910;
delete from tms_test_folder where id = 910;
delete from tms_quality_standard where id = 910;
delete from tms_project_sequence where project_id = 1 and entity_type = 'TEST_CASE';
