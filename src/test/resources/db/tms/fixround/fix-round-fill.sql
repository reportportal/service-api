-- Fixture of TestCaseFixRoundE2eIntegrationTest (project 1 = superadmin_personal): the Library folder the test
-- cases live in, the quality standard their grades are reported against and the GitHub Actions integration the
-- fix job is started with. The pipeline itself is not seeded: the first report of the scenario creates it.
insert into tms_test_folder (id, "name", description, project_id)
values (920, 'fix-round', 'tms/library/fixround', 1);

insert into tms_quality_standard (id, project_id, name, description)
values (920, 1, 'Test case quality standard', 'six criteria, sum of 100');

insert into tms_quality_standard_criterion (id, standard_id, name, max_points, sequence, criterion_key)
values (920, 920, 'Atomicity', 15, 1, 'atomicity'),
       (921, 920, 'Clear steps', 20, 2, 'clear_steps'),
       (922, 920, 'Clear expected results', 20, 3, 'clear_expected_results'),
       (923, 920, 'No invented logic', 20, 4, 'no_invented_logic'),
       (924, 920, 'No invented UI', 15, 5, 'no_invented_ui'),
       (925, 920, 'Coherence', 10, 6, 'coherence');

select setval('tms_quality_standard_id_seq', (select coalesce(max(id), 1) from tms_quality_standard));
select setval('tms_quality_standard_criterion_id_seq', (select coalesce(max(id), 1) from tms_quality_standard_criterion));

insert into integration (id, project_id, type, enabled, creator, creation_date, params, name)
values (920, 1, 9000, true, 'superadmin', now(),
        '{"params": {"token": "gh-test-token", "ref": "main", "repo": "org/repo", "fixWorkflowRef": "fix-test-case.yml"}}',
        'fix-round-github-actions');
