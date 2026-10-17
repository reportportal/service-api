-- Fixture of PipelineIterationE2eIntegrationTest (project 1 = superadmin_personal): the Library folder the
-- agent uploads into, and the project's quality standard the agent reports its scores against.
-- The pipeline itself is not seeded: the first report of the scenario creates it.
insert into tms_test_folder (id, "name", description, project_id)
values (910, 'iteration-1', 'tms/library/emptystate/iteration-1', 1);

insert into tms_quality_standard (id, project_id, name, description)
values (910, 1, 'Test case quality standard', 'six criteria, sum of 100');

insert into tms_quality_standard_criterion (id, standard_id, name, max_points, sequence, criterion_key)
values (910, 910, 'Atomicity', 15, 1, 'atomicity'),
       (911, 910, 'Clear steps', 20, 2, 'clear_steps'),
       (912, 910, 'Clear expected results', 20, 3, 'clear_expected_results'),
       (913, 910, 'No invented logic', 20, 4, 'no_invented_logic'),
       (914, 910, 'No invented UI', 15, 5, 'no_invented_ui'),
       (915, 910, 'Coherence', 10, 6, 'coherence');

select setval('tms_quality_standard_id_seq', (select coalesce(max(id), 1) from tms_quality_standard));
select setval('tms_quality_standard_criterion_id_seq', (select coalesce(max(id), 1) from tms_quality_standard_criterion));
