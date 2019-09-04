INSERT INTO public.launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode, status,
                           has_retries, rerun, approximate_duration)
VALUES (6, '6ccb1d60-5f6a-4d05-8ff2-87844aca75e6', 1, 1, 'Demo Api Tests', '### **Demonstration launch.**
A typical *Launch structure* comprises the following elements: Suite > Test > Step > Log.
Launch contains *randomly* generated `suites`, `tests`, `steps` with:
* random issues and statuses,
* logs,
* attachments with different formats.', '2019-08-29 08:37:34.468000', '2019-08-29 08:37:41.057000', 1, '2019-08-29 11:37:41.113000',
        'DEFAULT', 'FAILED', false, false, 0);

INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (37, 'd645e519-6d1b-418d-a18c-65e0386de81d', 'Launch Tests', null, 'SUITE', '2019-08-29 08:37:34.714000',
        'Here could be **very important information** about `test-cases` that are inside.', '2019-08-29 11:37:34.957000', '37',
        'auto:ceced8811816018fc63fdc8108f4fa02', true, false, true, null, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (44, '6cbbad11-4e16-4e3c-8e20-e92d95fd1ac4', 'LaunchStatusTest', null, 'TEST', '2019-08-29 08:37:34.876000',
        'This is a `test` level. Here you can handle *the aggregated information* per  `test`.', '2019-08-29 11:37:35.029000', '37.44',
        'auto:c490e23a31702cae558567606cfbcd16', true, false, true, 37, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (49, '2460b3ba-b039-4162-8159-f07454922eaf', 'before_class', null, 'BEFORE_CLASS', '2019-08-29 08:37:34.979000',
        'Greater or equals filter test for test items product bugs criteria. Negative value', '2019-08-29 11:37:35.030000', '37.44.49',
        'auto:024ed80924a60ee9ad654e1d1332a6d4', false, false, true, 44, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (52, 'dd5105e8-b8e5-4cc3-bdc5-71585a8d60f9', 'before_method', null, 'BEFORE_METHOD', '2019-08-29 08:37:35.252000', null,
        '2019-08-29 11:37:35.298000', '37.44.52', 'auto:950822a85767d38735fed9454f11c22d', false, false, true, 44, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (55, 'f3c84e8d-af4e-46b9-8ba7-f3d87e79fc9e', 'launchMixedItemsStatusText', null, 'STEP', '2019-08-29 08:37:35.352000', null,
        '2019-08-29 11:37:35.370000', '37.44.55', 'auto:e429b3e3b08bd6719de950aa38a4eee1', false, false, true, 44, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (56, '241baba5-8772-4fc6-b1b2-63267a587f4a', 'after_method', null, 'AFTER_METHOD', '2019-08-29 08:37:35.458000',
        'Greater or equals filter test for test items product bugs criteria. Negative value', '2019-08-29 11:37:35.474000', '37.44.56',
        'auto:3fb4be9706bfe8b2674d828c70dfa0b1', false, false, true, 44, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (57, 'f87bea9a-da86-4fa7-b60e-8583db706b4f', 'after_class', null, 'AFTER_CLASS', '2019-08-29 08:37:35.492000',
        'Greater or equals filter test for test items product bugs criteria. Negative value', '2019-08-29 11:37:35.505000', '37.44.57',
        'auto:ae5493ee1d67fc43007bfa35e3df4190', false, false, true, 44, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (58, '84778f7a-db06-490e-90dd-190116334f8e', 'UpdateLaunchTest', null, 'TEST', '2019-08-29 08:37:35.548000',
        'Here could be **very important information** about `test-cases` that are inside.', '2019-08-29 11:37:35.576000', '37.58',
        'auto:b8c3716f7a8e7fe45154106f429779ac', true, false, true, 37, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (59, '2c69ba9f-8439-4f8d-b5c0-90a49f6f4005', 'before_class', null, 'BEFORE_CLASS', '2019-08-29 08:37:35.564000', null,
        '2019-08-29 11:37:35.576000', '37.58.59', 'auto:1598bf2521d6536608c236f399148999', false, false, true, 58, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (60, '620eedeb-4381-4cd6-801a-907c5eda4f67', 'updateDefaultMode', null, 'STEP', '2019-08-29 08:37:35.592000',
        'This is the last **test case** of demo launch. There are only `logs` with `attachments` inside it.', '2019-08-29 11:37:35.605000',
        '37.58.60', 'auto:95851c62d74d8296fe185a76781bad74', false, false, true, 58, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (61, '717c3e04-4709-403b-8746-89b5300bbe39', 'after_class', null, 'AFTER_CLASS', '2019-08-29 08:37:35.680000',
        'This is the last **test case** of demo launch. There are only `logs` with `attachments` inside it.', '2019-08-29 11:37:35.692000',
        '37.58.61', 'auto:2a26611716b6199294d96bef5a7b95fe', false, false, true, 58, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (62, 'd758bd7c-1b38-4738-ac1d-37f3a81e5b56', 'FinishLaunchTest', null, 'TEST', '2019-08-29 08:37:35.720000',
        'Here could be **very important information** about `test-cases` that are inside.', '2019-08-29 11:37:35.746000', '37.62',
        'auto:e88e1e1bda69e11155a9696b3316172a', true, false, true, 37, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (63, '6756cd7b-95a3-4f65-9176-adb900158142', 'finishLaunch', null, 'STEP', '2019-08-29 08:37:35.737000', null,
        '2019-08-29 11:37:35.747000', '37.62.63', 'auto:dbaf84d7b0c758692da412fe481e6fe2', false, false, true, 62, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (92, '227face6-6049-4747-be1f-02ab16ad7103', 'after_method', null, 'AFTER_METHOD', '2019-08-29 08:37:40.342000', null,
        '2019-08-29 11:37:40.351000', '37.62.92', 'auto:6fa133d4dff8b98b9ceb657cc49e0321', false, false, true, 62, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (93, 'fa5aabaa-4eff-4a63-a1eb-b39f4c66365a', 'StartLaunchTest', null, 'TEST', '2019-08-29 08:37:40.384000',
        '**This is demonstration description.** This `test-item` contains automatically generated steps with logs and attachments.',
        '2019-08-29 11:37:40.407000', '37.93', 'auto:e28dda9a4e05d0b1d9379fd9782580cb', true, false, true, 37, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (94, '78f8424c-a5a6-472e-a92c-84057a05ebad', 'testCheckLaunchModeByDefault', null, 'STEP', '2019-08-29 08:37:40.398000', null,
        '2019-08-29 11:37:40.408000', '37.93.94', 'auto:df6d649a2d66c5a431abc70c9f3f546d', false, false, true, 93, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (95, '0d48ebaf-12a6-4dc2-af2a-d7ea4af7d06f', 'DeleteLaunchTest', null, 'TEST', '2019-08-29 08:37:40.936000', null,
        '2019-08-29 11:37:40.959000', '37.95', 'auto:6c242ef2e6d6ea1b41c7eb1157260c0a', true, false, true, 37, null, 6);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description, last_modified, path, unique_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id)
VALUES (96, 'ed393708-6e04-4484-842e-b0d4738180c0', 'deleteLaunchInProgress', null, 'STEP', '2019-08-29 08:37:40.949000', null,
        '2019-08-29 11:37:40.959000', '37.95.96', 'auto:e2460aaadd39267213b129a08545f378', false, false, true, 95, null, 6);

INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (49, 'SKIPPED', '2019-08-29 08:37:35.040000', 0.061);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (52, 'PASSED', '2019-08-29 08:37:35.304000', 0.052);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (55, 'PASSED', '2019-08-29 08:37:35.440000', 0.088);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (56, 'PASSED', '2019-08-29 08:37:35.478000', 0.02);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (57, 'FAILED', '2019-08-29 08:37:35.509000', 0.017);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (44, 'SKIPPED', '2019-08-29 08:37:35.528000', 0.652);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (59, 'PASSED', '2019-08-29 08:37:35.580000', 0.016);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (60, 'FAILED', '2019-08-29 08:37:35.663000', 0.071);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (61, 'PASSED', '2019-08-29 08:37:35.696000', 0.016);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (58, 'PASSED', '2019-08-29 08:37:35.707000', 0.159);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (63, 'FAILED', '2019-08-29 08:37:40.325000', 4.588);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (92, 'FAILED', '2019-08-29 08:37:40.355000', 0.013);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (62, 'FAILED', '2019-08-29 08:37:40.370000', 4.65);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (94, 'PASSED', '2019-08-29 08:37:40.913000', 0.515);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (93, 'FAILED', '2019-08-29 08:37:40.925000', 0.541);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (96, 'FAILED', '2019-08-29 08:37:40.999000', 0.05);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (95, 'FAILED', '2019-08-29 08:37:41.015000', 0.079);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (37, 'FAILED', '2019-08-29 08:37:41.026000', 6.312);

INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (26, 'build', 'flaky', 37, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (61, 'build', 'most failed', 56, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (44, 'platform', 'longest', 44, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (280, 'platform', 'arch', 93, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (8, null, 'demo', null, 6, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (13, 'build', '3.29.11.0', null, 6, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (16, 'platform', 'arch', null, 6, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (23, 'os', 'android', 37, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (28, 'os', 'ios', 37, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (41, null, 'most stable', 44, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (43, null, 'most failed', 44, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (52, null, 'most stable', 49, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (53, null, 'most failed', 49, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (54, null, 'longest', 49, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (62, null, 'longest', 56, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (63, null, 'flaky', 56, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (64, null, 'most failed', 57, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (65, 'os', 'android', 57, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (66, null, 'flaky', 57, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (67, null, 'most failed', 58, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (68, 'os', 'android', 58, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (69, null, 'flaky', 58, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (70, 'os', 'android', 60, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (71, null, 'api', 60, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (72, 'os', 'ios', 60, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (73, 'os', 'android', 61, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (74, null, 'flaky', 61, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (75, 'os', 'ios', 61, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (76, null, 'most failed', 62, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (77, 'os', 'android', 62, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (78, null, 'flaky', 62, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (133, 'os', 'android', 93, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (134, null, 'flaky', 93, null, false);
INSERT INTO public.item_attribute (id, key, value, item_id, launch_id, system)
VALUES (135, 'os', 'ios', 93, null, false);

INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (1, false, 'superadmin', 1);
INSERT INTO public.filter (id, name, target, description) VALUES (1, 'New_filter', 'Launch', null);
INSERT INTO public.filter_condition (id, filter_id, condition, value, search_criteria, negative) VALUES (1, 1, 'IN', '6,8,9', 'id', false);
INSERT INTO public.filter_sort (id, filter_id, field, direction) VALUES (1, 1, 'name', 'ASC');

INSERT INTO public.shareable_entity (id, shared, owner, project_id) VALUES (2, false, 'superadmin', 1);
INSERT INTO public.widget (id, name, description, widget_type, items_count, widget_options) VALUES (2, 'health', null, 'componentHealthCheck', 10, '{"options": {"latest": ""}}');
INSERT INTO public.content_field (id, field) VALUES (2, 'build');
INSERT INTO public.content_field (id, field) VALUES (2, 'platform');
INSERT INTO public.content_field (id, field) VALUES (2, 'os');

INSERT INTO public.widget_filter (widget_id, filter_id) VALUES (2, 1);
