-- Minimal TMS test folder/test case used only to exercise Pipeline's link-by-displayId
-- ingest flow. Project 1 = superadmin_personal, project 2 = default_personal (same
-- convention as src/test/resources/db/tms/tms-test-case/tms-test-case-fill.sql).
insert into tms_test_folder (id, "name", description, project_id)
values (900, 'Pipeline Folder', 'Folder for pipeline integration tests', 1);

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (900, 'Pipeline Linked Test Case', 'Linked from a pipeline stage', 900, 'HIGH', 1, 'TC-900');

insert into tms_test_case (id, "name", description, test_folder_id, priority, project_id, display_id)
values (901, 'Pipeline Second Test Case', 'Another linked test case', 900, 'MEDIUM', 1, 'TC-901');

-- Pipeline definitions
insert into pipeline (id, project_id, name, type, description, attributes, settings, created_at)
values (900, 1, 'factory-tc-gen', 'TEST_CASE_GENERATION', 'Factory pipeline: agents convert requirements into test cases',
        '[{"key": "repo", "label": "Repository", "value": "org/repo", "type": "MAIN", "valueType": "TEXT"}]',
        null, '2026-08-28 16:40:00');

insert into pipeline (id, project_id, name, description, created_at)
values (901, 2, 'other-project-pipeline', 'Belongs to a different project', '2026-08-28 16:40:00');

-- A second pipeline definition in the SAME project, purely so the "compare must be same
-- pipeline definition" guard has something legitimate to reject.
insert into pipeline (id, project_id, name, description, created_at)
values (902, 1, 'other-pipeline-same-project', 'A different pipeline definition, same project', '2026-08-28 16:40:00');

-- Iterations for pipeline 900 (two, so compare/list have something to work with).
-- Attributes are stored as a plain JSON array of typed items; search_text / attribute_index are the
-- values the application derives from them on write.
insert into pipeline_iteration (id, pipeline_id, iteration_number, status, status_category, attributes, search_text, attribute_index,
                                started_at, finished_at, rerun, created_by, created_at)
values (900, 900, 1, 'Completed', 'PASSED',
        '[{"key": "requirement", "label": "Requirement", "value": "US-TMS-EXP-002", "type": "MAIN", "valueType": "TEXT", "identity": true},
          {"key": "trigger", "label": "Trigger", "value": "CI - booking pack", "type": "MAIN", "valueType": "TEXT"},
          {"key": "suiteScore", "label": "Suite score", "value": "86", "type": "MAIN", "valueType": "SCORE", "direction": "HIGHER_BETTER"},
          {"key": "cost", "label": "Cost", "value": "0.61", "type": "MAIN", "valueType": "MONEY", "direction": "LOWER_BETTER"},
          {"key": "env", "value": "beta5", "type": "TAG", "valueType": "TEXT"},
          {"key": "ci", "value": "#1284551", "type": "TAG", "valueType": "LINK", "url": "https://ci.example/runs/1284551"}]',
        'us-tms-exp-002 ci - booking pack 86 0.61 beta5 #1284551',
        chr(1) || 'requirement' || chr(2) || 'us-tms-exp-002' || chr(1) || chr(1) || 'env' || chr(2) || 'beta5' || chr(1),
        '2026-09-01 09:00:00', '2026-09-01 09:05:00', false, 1, '2026-09-01 09:05:00');

insert into pipeline_iteration (id, pipeline_id, iteration_number, status, status_category, attributes, search_text, attribute_index,
                                started_at, finished_at, rerun, created_by, created_at)
values (901, 900, 2, 'In review', 'WAITING',
        '[{"key": "requirement", "label": "Requirement", "value": "US-TMS-EXP-002", "type": "MAIN", "valueType": "TEXT", "identity": true},
          {"key": "trigger", "label": "Trigger", "value": "CI - booking pack retry", "type": "MAIN", "valueType": "TEXT"},
          {"key": "suiteScore", "label": "Suite score", "value": "72", "type": "MAIN", "valueType": "SCORE", "direction": "HIGHER_BETTER"},
          {"key": "cost", "label": "Cost", "value": "0.98", "type": "MAIN", "valueType": "MONEY", "direction": "LOWER_BETTER"}]',
        'us-tms-exp-002 ci - booking pack retry 72 0.98',
        chr(1) || 'requirement' || chr(2) || 'us-tms-exp-002' || chr(1),
        '2026-09-03 14:22:00', '2026-09-03 14:26:12', false, 1, '2026-09-03 14:26:12');

-- Iteration for the other project's pipeline, to prove project isolation
insert into pipeline_iteration (id, pipeline_id, iteration_number, status, status_category, rerun, created_at)
values (910, 901, 1, 'Completed', 'PASSED', false, '2026-09-01 09:05:00');

-- Iteration for the same-project-different-pipeline definition (compare-must-match guard)
insert into pipeline_iteration (id, pipeline_id, iteration_number, status, status_category, rerun, created_at)
values (920, 902, 1, 'Completed', 'PASSED', false, '2026-09-01 09:05:00');

-- Stages (path = own id: these fixture rows are all top-level, no nesting)
insert into pipeline_stage (id, iteration_id, stage_key, name, sequence, path, status, status_category, attributes, sections)
values (900, 900, 'stage-gen-tc', 'MD -> Test cases', 1, '900', 'Passed', 'PASSED',
        '[{"key": "cases", "label": "cases", "value": "4", "type": "MAIN", "valueType": "NUMBER"},
          {"key": "agent", "value": "create-test-cases@0.4", "type": "TAG", "valueType": "TEXT"}]',
        '[{"key": "grades", "title": "Grades",
           "columns": [{"key": "case", "label": "Test case", "valueType": "TEXT"}, {"key": "score", "label": "Score", "valueType": "SCORE"}],
           "rows": [{"key": "TC-900", "cells": {"case": {"value": "Pipeline Linked Test Case", "target": {"entityType": "TEST_CASE", "entityId": "TC-900"}},
                                                "score": {"value": "86"}}}]}]');

insert into pipeline_stage (id, iteration_id, stage_key, name, sequence, path, status, status_category, attributes, execution)
values (901, 901, 'stage-gen-tc', 'MD -> Test cases', 1, '901', 'Needs human', 'WAITING',
        '[{"key": "agent", "value": "create-test-cases@0.4", "type": "TAG", "valueType": "TEXT"}]',
        '{"executorType": "GITHUB_ACTIONS", "externalRunId": "78911",
          "runUrl": "https://github.com/org/repo/actions/runs/123456",
          "retryable": true, "retryParameters": {"repo": "org/repo", "workflowRef": "ai-pipeline.yml"}}');

insert into pipeline_stage (id, iteration_id, stage_key, name, sequence, path, status, status_category, attributes)
values (902, 901, 'stage-upload', 'Upload to Library', 2, '902', 'Passed', 'PASSED',
        '[{"key": "agent", "value": "upload-tc@1.0", "type": "TAG", "valueType": "TEXT"}]');

-- Link stage 900 to the existing TMS test case, so the sections and Review progress have a case to point at
insert into pipeline_stage_test_case (id, stage_id, test_case_id)
values (900, 900, 900);

-- CI-trigger integration for the GitHub Actions retry test (type 9000 seeded by V226, group AUTOMATION)
insert into integration (id, project_id, type, enabled, creator, creation_date, params, name)
values (900, 1, 9000, true, 'superadmin', now(),
        '{"params": {"token": "gh-test-token", "ref": "main", "repo": "org/repo"}}', 'pipeline-github-actions');

select setval('pipeline_id_seq', (select coalesce(max(id), 1) from pipeline));
select setval('pipeline_iteration_id_seq', (select coalesce(max(id), 1) from pipeline_iteration));
select setval('pipeline_stage_id_seq', (select coalesce(max(id), 1) from pipeline_stage));
select setval('pipeline_stage_test_case_id_seq', (select coalesce(max(id), 1) from pipeline_stage_test_case));
select setval('tms_test_folder_id_seq', (select coalesce(max(id), 1) from tms_test_folder));
select setval('tms_test_case_id_seq', (select coalesce(max(id), 1) from tms_test_case));
select setval('integration_id_seq', (select coalesce(max(id), 1) from integration));

insert into tms_project_sequence (project_id, entity_type, current_value)
values (1, 'TEST_CASE', 901)
on conflict (project_id, entity_type) do update set current_value = excluded.current_value;
