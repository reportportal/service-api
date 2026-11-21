-- Removes what Automate tests leave behind; they run without a rolled-back test transaction (see
-- TmsAutomationIntegrationTest). Runs before pipeline-cleanup.sql. Deleting the pipeline takes its iterations and,
-- through them, the automation runs along.
delete from tms_test_case_automation_run where project_id = 1;
delete from pipeline where project_id = 1 and type = 'TEST_AUTOMATION';
delete from integration where id = 910;
delete from tms_test_case_version where id in (9000, 9001);
