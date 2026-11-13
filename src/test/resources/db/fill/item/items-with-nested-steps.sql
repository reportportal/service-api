INSERT INTO public.launch (id, uuid, project_id, user_id, name, description, start_time, end_time,
                           number, last_modified, mode, status, has_retries, rerun,
                           approximate_duration)
VALUES (10, '4c838392-ba6d-48b0-b3b2-213c3a5eeebf', 1, 1, 'superadmin_TEST_EXAMPLE', null,
        '2020-02-12 16:17:58.041000', '2020-02-12 16:18:00.141000', 2, '2020-02-12 19:21:00.758721',
        'DEFAULT', 'FAILED', false, false, 0.924);

INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (130, '53e7a3b7-4c25-4948-8fd1-8078a105a912', 'Default Suite', null, 'SUITE',
        '2020-02-12 16:17:58.701000', null, '2020-02-12 19:20:59.069036', '130',
        'auto:ac4756dc4f6593ae95b34f92d8190314', null, true, false, true, null, null, 10,
        -837850887);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (131, '81a497ed-9889-41c7-bbf1-63c228118625', 'examples-java',
        'com.epam.reportportal.example.testng.logback.step.NestedStepsTest', 'TEST',
        '2020-02-12 16:17:58.790000', null, '2020-02-12 19:20:59.100846', '130.131',
        'auto:e4c9be3a695d687202ce46bb0248c3be', null, true, false, true, 130, null, 10,
        -535955601);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (132, '2d497d02-0925-47d5-b8cf-7a7503a95e3a', 'orderProductsTest',
        'com.epam.reportportal.example.testng.logback.step.NestedStepsTest.orderProductsTest',
        'STEP', '2020-02-12 16:17:58.883000', '', '2020-02-12 19:20:59.101439', '130.131.132',
        'auto:8cbc957f34516c324711053cd24070c9',
        'com.epam.reportportal.example.testng.logback.step.NestedStepsTest.orderProductsTest[]',
        false, false, true, 131, null, 10, -186468562);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (133, 'd82da671-4dac-46c6-884f-01527b58a2b7', 'navigateToMainPage', null, 'STEP',
        '2020-02-12 16:17:58.916000', null, '2020-02-12 19:20:59.134683', '130.131.132.133',
        'auto:847af698ac7d55fdcdae9a992b40e476', null, false, false, false, 132, null, 10,
        901412180);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (134, '0d89cecd-0d60-4664-b55c-a7bf93d1d0fd', 'login', null, 'STEP',
        '2020-02-12 16:17:58.930000', null, '2020-02-12 19:20:59.169801', '130.131.132.134',
        'auto:41ab6a490fe3702fad48163769f2e733', null, false, false, false, 132, null, 10,
        -375485253);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (135, '63fef110-d983-45ec-a6a4-6141c5e50609', 'navigateToProductsPage', null, 'STEP',
        '2020-02-12 16:17:58.932000', null, '2020-02-12 19:20:59.190198', '130.131.132.135',
        'auto:ba8e9eb5134bea607b54d6417f2d11ba', null, false, false, false, 132, null, 10,
        1300291829);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (136, '9f5cf143-f0b6-4b8d-845c-6431b3ed7c98', 'Add 5 products to the cart', null, 'STEP',
        '2020-02-12 16:17:58.951000', null, '2020-02-12 19:20:59.274305', '130.131.132.136',
        'auto:1fcfd21fe319ea5d1ab8207434f1f166', null, false, false, false, 132, null, 10,
        -472716353);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (137, 'ce28885c-ca32-4a1c-9ca4-5f12a5fe29b8', 'Payment step with price = 15.0', null, 'STEP',
        '2020-02-12 16:17:59.004000', null, '2020-02-12 19:20:59.293817', '130.131.132.137',
        'auto:0da71f15f42cf1488de7f4b42258ba7d', null, false, false, false, 132, null, 10,
        1121458889);
INSERT INTO public.test_item (item_id, uuid, name, code_ref, type, start_time, description,
                              last_modified, path, unique_id, test_case_id, has_children,
                              has_retries, has_stats, parent_id, retry_of, launch_id,
                              test_case_hash)
VALUES (138, '7e54aa20-4fe3-4fa6-a181-7b4f00cd406e', 'logout', null, 'STEP',
        '2020-02-12 16:17:59.009000', null, '2020-02-12 19:20:59.374424', '130.131.132.138',
        'auto:8a16491d5f008de1d9b9df9e2d60d94c', null, false, false, false, 132, null, 10,
        -125693020);

INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (131, 'FAILED', '2020-02-12 16:18:00.079000', 1.289);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (132, 'FAILED', '2020-02-12 16:17:59.027000', 0.144);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (133, 'PASSED', '2020-02-12 16:17:58.917000', 0.001);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (134, 'PASSED', '2020-02-12 16:17:58.931000', 0.001);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (135, 'PASSED', '2020-02-12 16:17:58.938000', 0.006);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (136, 'PASSED', '2020-02-12 16:17:58.992000', 0.041);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (137, 'PASSED', '2020-02-12 16:17:59.005000', 0.001);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (138, 'FAILED', '2020-02-12 16:17:59.022000', 0.013);

INSERT INTO public.issue (issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (132, 1, null, false, false);

INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (142, '0da5f29e-8b4d-45e6-b918-6720774a2386', '2020-02-12 16:17:59.142000', '[rp-io-7] - Start test item successfully completed
', 130, null, '2020-02-12 19:21:00.491728', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (44, '2c353d0d-405a-4326-8651-969b1fab118a', '2020-02-12 16:17:59.061000', '[rp-io-3] - Start test item successfully completed
', 130, null, '2020-02-12 19:20:59.326132', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (45, 'c1c07151-99b3-477c-bdfd-971f76615651', '2020-02-12 16:17:59.061000', '[rp-io-3] - Starting test item...rp-io-3
', 130, null, '2020-02-12 19:20:59.339195', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (46, '673679a3-92be-44ee-b87b-2a4abe0702a1', '2020-02-12 16:17:59.062000', '[rp-io-3] - Starting test item...rp-io-3
', 130, null, '2020-02-12 19:20:59.351817', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (47, '95ac257a-dc8c-44f5-827f-639eb083f696', '2020-02-12 16:17:59.069000', '[rp-io-3] - Starting test item...rp-io-3
', 130, null, '2020-02-12 19:20:59.387035', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (48, '236406fe-4706-4ec5-9602-3eca7a972ab5', '2020-02-12 16:17:59.081000', '[rp-io-3] - Starting test item...rp-io-3
', 130, null, '2020-02-12 19:20:59.400567', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (135, '19db17e3-045a-4bea-895d-ae4b68931585', '2020-02-12 16:17:59.120000', '[rp-io-6] - ReportPortal item with ID ''63fef110-d983-45ec-a6a4-6141c5e50609'' has been created
', 130, null, '2020-02-12 19:21:00.425099', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (136, '606c1696-14e1-4fde-9a1f-1eeed8cecf0f', '2020-02-12 16:17:59.141000', '[rp-io-3] - Starting test item...rp-io-3
', 130, null, '2020-02-12 19:21:00.432653', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (137, '0982b221-1bdd-4661-943b-bf7633fc8350', '2020-02-12 16:17:59.141000', '[rp-io-8] - ReportPortal item with ID ''ce28885c-ca32-4a1c-9ca4-5f12a5fe29b8'' has been created
', 130, null, '2020-02-12 19:21:00.441895', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (138, '0e57bf38-4374-4d08-9790-70695aacce06', '2020-02-12 16:17:59.142000', '[rp-io-6] - Start test item successfully completed
', 130, null, '2020-02-12 19:21:00.452270', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (139, '7b72540e-f2a1-4d7d-a982-40353f86e492', '2020-02-12 16:17:59.142000', '[rp-io-8] - Start test item successfully completed
', 130, null, '2020-02-12 19:21:00.458125', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (140, 'f46438d9-5029-4e33-9f26-5d938f0adb8e', '2020-02-12 16:17:59.142000', '[rp-io-5] - Start test item successfully completed
', 130, null, '2020-02-12 19:21:00.462681', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (144, '2d469e6f-f292-43ff-8829-38abf2941906', '2020-02-12 16:17:59.939000', '[rp-io-59] - Starting test item...rp-io-59
', 130, null, '2020-02-12 19:21:00.518662', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (43, '8bb0f8df-c45c-4e23-a899-cae4f6bc467c', '2020-02-12 16:17:59.060000', '[rp-io-3] - ReportPortal item with ID ''2d497d02-0925-47d5-b8cf-7a7503a95e3a'' has been created
', 130, null, '2020-02-12 19:20:59.314594', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (143, '51c21ea7-fb6d-4b5a-b9c4-3e7586e7cf0d', '2020-02-12 16:17:59.144000', '[rp-io-7] - Starting test item...rp-io-7
', 130, null, '2020-02-12 19:21:00.507512', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (56, 'e68e3e52-93b6-4444-a494-7eaefad0582a', '2020-02-12 16:17:59.115000', '[rp-io-5] - ReportPortal item with ID ''0d89cecd-0d60-4664-b55c-a7bf93d1d0fd'' has been created
', 130, null, '2020-02-12 19:20:59.482912', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (54, 'e5bc33aa-795f-416b-b4d5-f84ee3fade8c', '2020-02-12 16:17:59.098000', '[rp-io-4] - Start test item successfully completed
', 130, null, '2020-02-12 19:20:59.471961', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (52, '36102d3d-2db9-434d-8b12-d20a94cf966f', '2020-02-12 16:17:59.096000', '[rp-io-4] - ReportPortal item with ID ''d82da671-4dac-46c6-884f-01527b58a2b7'' has been created
', 130, null, '2020-02-12 19:20:59.453755', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (132, 'f280e4b6-dbc9-47f6-8a61-33a8b12b0f67', '2020-02-12 16:17:59.127000', '[rp-io-7] - ReportPortal item with ID ''9f5cf143-f0b6-4b8d-845c-6431b3ed7c98'' has been created
', 130, null, '2020-02-12 19:21:00.404225', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (133, '5f74f81e-2d72-4c61-af06-e303148d49bf', '2020-02-12 16:17:59.979000', 'java.lang.NullPointerException: Oops
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
', 130, null, '2020-02-12 19:21:00.409335', 40000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (50, 'e731e69f-337a-45b7-a311-8dc02bc2a632', '2020-02-12 16:17:59.083000', '[rp-io-3] - Starting test item...rp-io-3
', 130, null, '2020-02-12 19:20:59.421944', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (62, 'cb0a02e8-bf83-4e81-8239-ed068a66bf5d', '2020-02-12 16:17:59.036000', 'java.lang.NullPointerException: Oops
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
', 131, null, '2020-02-12 19:20:59.554493', 40000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (61, 'd91bf47c-8885-425f-ae68-293d15c7c88f', '2020-02-12 16:17:58.899000', '[rp-io-0] - ReportPortal launch with ID ''4c838392-ba6d-48b0-b3b2-213c3a5eeebf'' has been created
', 131, null, '2020-02-12 19:20:59.542107', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (58, '84da4033-b8c7-4124-90e5-7240f132fdeb', '2020-02-12 16:17:59.012000', '[rp-io-2] - Start test item successfully completed
', 132, null, '2020-02-12 19:20:59.494083', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (57, '7e514e2d-2c09-4e89-9bff-9a113d1d005a', '2020-02-12 16:17:59.012000', '[rp-io-2] - ReportPortal item with ID ''81a497ed-9889-41c7-bbf1-63c228118625'' has been created
', 132, null, '2020-02-12 19:20:59.488275', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (53, '34072200-1894-48f6-ad21-6f62dc779ce7', '2020-02-12 16:17:58.905000', '[rp-io-0] - Launch start successfully completed
', 132, null, '2020-02-12 19:20:59.460121', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (55, '3c14aa9f-8c8e-4e64-aa4c-b0fc79f1360a', '2020-02-12 16:17:58.997000', '[rp-io-17] - Logging context completed
', 132, null, '2020-02-12 19:20:59.477828', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (59, 'a61a609e-b9f1-4d12-a4ae-ea98f98e2a15', '2020-02-12 16:17:59.013000', '[rp-io-2] - Starting test item...rp-io-2
', 132, null, '2020-02-12 19:20:59.498580', 10000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (42, 'ffc855e9-0e78-48da-bf49-c5a037a36eba', '2020-02-12 16:17:58.916000', '[main] - Main page displayed
', 133, null, '2020-02-12 19:20:59.305525', 20000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (51, '2b2d89bd-8ce4-4a25-b591-180276ebd08d', '2020-02-12 16:17:58.931000', '[main] - User logged in
', 134, null, '2020-02-12 19:20:59.441288', 20000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (60, 'a832ed04-1cac-4676-bf40-ae2e9ccfe734', '2020-02-12 16:17:58.935000', '[main] - Products page opened
', 135, null, '2020-02-12 19:20:59.513959', 20000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (49, '302e659a-f67f-410d-b28a-e65766d2a1e6', '2020-02-12 16:17:59.004000', '[main] - Successful payment
', 137, null, '2020-02-12 19:20:59.409029', 20000, null, 1);
INSERT INTO public.log (id, uuid, log_time, log_message, item_id, launch_id, last_modified,
                        log_level, attachment_id, project_id)
VALUES (63, '3e6e5b42-9311-4417-afd4-f63241c55660', '2020-02-12 16:17:59.217000', 'java.lang.NullPointerException: Oops
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
', 138, null, '2020-02-12 19:20:59.560813', 40000, null, 1);