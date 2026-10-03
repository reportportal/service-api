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
insert into pipeline (id, project_id, name, description, auto_ready_enabled, auto_ready_threshold, created_at)
values (900, 1, 'factory-tc-gen', 'Factory pipeline: agents convert requirements into test cases', false, null, '2026-08-28 16:40:00');

insert into pipeline (id, project_id, name, description, auto_ready_enabled, auto_ready_threshold, created_at)
values (901, 2, 'other-project-pipeline', 'Belongs to a different project', false, null, '2026-08-28 16:40:00');

-- A second pipeline definition in the SAME project, purely so the "compare must be same
-- pipeline definition" guard has something legitimate to reject.
insert into pipeline (id, project_id, name, description, auto_ready_enabled, auto_ready_threshold, created_at)
values (902, 1, 'other-pipeline-same-project', 'A different pipeline definition, same project', false, null, '2026-08-28 16:40:00');

-- Iterations for pipeline 900 (two, so compare/list have something to work with)
-- NOTE: PipelineMetrics/PipelineAttributes are Hibernate JsonbUserType wrappers around a
-- single named field ("metrics"/"attributes") -- Jackson serializes the WHOLE wrapper, so the
-- real on-disk shape is {"metrics": {...}} / {"attributes": {...}}, not a flat map. A flat map
-- here silently deserializes to a null wrapper field (FAIL_ON_UNKNOWN_PROPERTIES is off).
insert into pipeline_iteration (id, pipeline_id, iteration_number, status, metrics, attributes, trigger, started_at, finished_at, rerun, created_by, created_at)
values (900, 900, 1, 'PASSED', '{"metrics": {"avgScore": 86, "costUsd": 0.61}}', '{"attributes": {"env": "beta5", "ci": "#1284551"}}', 'CI - booking pack', '2026-09-01 09:00:00', '2026-09-01 09:05:00', false, 1, '2026-09-01 09:05:00');

insert into pipeline_iteration (id, pipeline_id, iteration_number, status, metrics, trigger, started_at, finished_at, rerun, created_by, created_at)
values (901, 900, 2, 'NEEDS_HUMAN', '{"metrics": {"avgScore": 72, "costUsd": 0.98}}', 'CI - booking pack retry', '2026-09-03 14:22:00', '2026-09-03 14:26:12', false, 1, '2026-09-03 14:26:12');

-- Iteration for the other project's pipeline, to prove project isolation
insert into pipeline_iteration (id, pipeline_id, iteration_number, status, metrics, rerun, created_at)
values (910, 901, 1, 'PASSED', '{"metrics": {"avgScore": 95}}', false, '2026-09-01 09:05:00');

-- Iteration for the same-project-different-pipeline definition (compare-must-match guard)
insert into pipeline_iteration (id, pipeline_id, iteration_number, status, metrics, rerun, created_at)
values (920, 902, 1, 'PASSED', '{"metrics": {"avgScore": 99}}', false, '2026-09-01 09:05:00');

-- Stages (path = own id: these fixture rows are all top-level, no nesting)
insert into pipeline_stage (id, iteration_id, stage_key, name, short_name, sequence, path, status, metrics, attributes, retryable)
values (900, 900, 'stage-gen-tc', 'MD -> Test cases', 'Gen TC', 1, '900', 'PASSED',
        '{"metrics": {"softPct": 94, "costUsd": 0.28}}',
        '{"attributes": {"agent": "create-test-cases@0.4"}}',
        false);

insert into pipeline_stage (id, iteration_id, stage_key, name, short_name, sequence, path, status, metrics, attributes,
                             ci_provider, ci_repo, ci_workflow_ref, ci_run_id, ci_job_id, ci_run_url, retryable)
values (901, 901, 'stage-gen-tc', 'MD -> Test cases', 'Gen TC', 1, '901', 'NEEDS_HUMAN',
        '{"metrics": {"softPct": 72, "costUsd": 0.98}}',
        '{"attributes": {"agent": "create-test-cases@0.4"}}',
        'GITHUB_ACTIONS', 'org/repo', 'ai-pipeline.yml', '123456', '78911',
        'https://github.com/org/repo/actions/runs/123456', true);

insert into pipeline_stage (id, iteration_id, stage_key, name, sequence, path, status, attributes, retryable)
values (902, 901, 'stage-upload', 'Upload to Library', 2, '902', 'PASSED', '{"attributes": {"agent": "upload-tc@1.0"}}', false);

-- Link stage 900 to the existing TMS test case, so detail/compare responses have testCaseIds
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
