-- Project 2 (default_personal) already has a quality standard defined, so GET/PUT/DELETE
-- have something to work with. Project 1 (superadmin_personal) deliberately has none, so
-- POST-create and the "not found" GET/PUT paths have something to exercise too.
insert into tms_quality_standard (id, project_id, name, description)
values (900, 2, 'Default TC quality rubric', '6-criteria, sum/100');

insert into tms_quality_standard_criterion (id, standard_id, name, max_points, sequence)
values (900, 900, 'Scenario correctness', 60, 1);

insert into tms_quality_standard_criterion (id, standard_id, name, max_points, sequence)
values (901, 900, 'Step clarity', 40, 2);

select setval('tms_quality_standard_id_seq', (select coalesce(max(id), 1) from tms_quality_standard));
select setval('tms_quality_standard_criterion_id_seq', (select coalesce(max(id), 1) from tms_quality_standard_criterion));
