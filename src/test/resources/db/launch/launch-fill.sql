INSERT INTO launch (id, uuid, project_id, user_id, name, description, start_time, end_time, number, last_modified, mode,
                    status)
VALUES (1,
        '4b02ef3a-56d6-443b-8cf7-27014bd53497',
        2,
        2,
        'Test launch 2',
        'postman updated',
        now() - interval '1 hour',
        now(),
        1,
        now(),
        'DEFAULT',
        'PASSED'),
       (2,
        '4850a659-ac26-4a65-8ea4-a6756a57fb92',
        2,
        2,
        'Test launch 1',
        'postman updated',
        now() - interval '1 hour',
        now(),
        1,
        now(),
        'DEFAULT',
        'FAILED'),
       (3, 'befef834-b2ef-4acf-aea3-b5a5b15fd93c', 2, 2, 'empty launch', 'postman', now(), null, 1, now(), 'DEFAULT',
        'IN_PROGRESS'),
       (4, '2e13b3df-298b-4052-beb8-426eedbc38ee', 1, 1, 'empty debug launch', 'postman', now(), null, 1, now(),
        'DEBUG', 'IN_PROGRESS'),
       (5, 'e3adc64e-87cc-4781-b2d3-faa4ef1679dc', 2, 2, 'empty launch 2', 'postman', now(), null, 1, now(), 'DEFAULT',
        'IN_PROGRESS');

INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (1,
        'Test suite',
        'SUITE',
        now(),
        'Test suite root item',
        now(),
        '1',
        'auto:5cec611c1def5b9ca8c88a53085823d6',
        true,
        false,
        null,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (2,
        'Test item 1',
        'TEST',
        now(),
        'description on finish',
        now(),
        '1.2',
        'auto:d129a83d1af8eb240b771e1119321fc3',
        true,
        false,
        1,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (3,
        'Step item 1',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.2.3',
        'auto:6c266a54c332b0c39a5ce990b5921281',
        false,
        false,
        2,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (4,
        'After method 1',
        'AFTER_METHOD',
        now(),
        'description on finish',
        now(),
        '1.2.4',
        'auto:a9e91649327d2f79168f64a9ff406944',
        false,
        false,
        2,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (5,
        'Step item 2',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.2.5',
        'auto:492d4b3430281961065b799c7d76c04e',
        false,
        false,
        2,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (6,
        'Test item 2',
        'TEST',
        now(),
        'description on finish',
        now(),
        '1.6',
        'auto:2149643aeb864f8e1770c2f0c888a0ab',
        true,
        false,
        1,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (14,
        'Test suite',
        'SUITE',
        now(),
        'Test suite root item',
        now(),
        '14',
        'auto:6bac3cae67ef43f2aa252b36a8d246cd',
        true,
        false,
        null,
        null,
        2);


INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (7,
        'Step item 3',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.6.7',
        'auto:f6111822fd8162dd550993ae25cafae3',
        false,
        false,
        6,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (8,
        'Step item 4',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.6.8',
        'auto:96e29892f3ba729ad60bab56d8713561',
        false,
        false,
        6,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (9,
        'Test item 3',
        'TEST',
        now(),
        'description on finish',
        now(),
        '1.9',
        'auto:a87a50cedf089c88f49d45ea15778088',
        true,
        false,
        1,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (10,
        'before method item 5',
        'BEFORE_METHOD',
        now(),
        'description on finish',
        now(),
        '1.9.10',
        'auto:48083753cbf111daf6288d958f5f59b7',
        false,
        false,
        9,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (11,
        'Step item 5',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.9.11',
        'auto:efe6cd6713e7cc5bddfb9d863c15b849',
        false,
        false,
        9,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (12,
        'Step item 6',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.9.12',
        'auto:5db0a0154347eb920b15273ab6b9c659',
        false,
        false,
        9,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (13,
        'Step item 7',
        'STEP',
        now(),
        'description on finish',
        now(),
        '1.9.13',
        'auto:8a29adf0e0f2038112083886c1eda1d4',
        false,
        false,
        9,
        null,
        1);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (15,
        'Test item 1',
        'TEST',
        now(),
        'description on finish',
        now(),
        '14.15',
        'auto:3660c42119848125091d3daaa3fc212a',
        true,
        false,
        14,
        null,
        2);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (16,
        'Step item 1',
        'STEP',
        now(),
        'description on finish',
        now(),
        '14.15.16',
        'auto:6cd23d5d2ccffa4513e0002eee883612',
        false,
        false,
        15,
        null,
        2);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (17,
        'Step item 2',
        'STEP',
        now(),
        'description on finish',
        now(),
        '14.15.17',
        'auto:4c5d69ac17f1bc8dcfb9fff98e943e0a',
        false,
        false,
        15,
        null,
        2);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (18,
        'Step item 3',
        'STEP',
        now(),
        'description on finish',
        now(),
        '14.15.18',
        'auto:6e318b7454d517d02183cf1faa0d91f1',
        false,
        false,
        15,
        null,
        2);
INSERT INTO public.test_item (item_id,
                              name,
                              type,
                              start_time,
                              description,
                              last_modified,
                              path,
                              unique_id,
                              has_children,
                              has_retries,
                              parent_id,
                              retry_of,
                              launch_id)
VALUES (19,
        'Step item 4',
        'STEP',
        now(),
        'description on finish',
        now(),
        '14.15.19',
        'auto:f333c9d50992fa1b72d889f38af357f4',
        false,
        false,
        15,
        null,
        2);


INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (3, 'PASSED', now(), 0.198);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (4, 'PASSED', now(), 0.159);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (5, 'PASSED', now(), 0.144);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (2, 'PASSED', now(), 1.1);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (7, 'PASSED', now(), 0.152);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (8, 'PASSED', now(), 0.182);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (6, 'PASSED', now(), 0.721);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (10, 'PASSED', now(), 0.149);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (11, 'PASSED', now(), 0.142);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (12, 'PASSED', now(), 0.145);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (13, 'PASSED', now(), 0.129);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (9, 'PASSED', now(), 1.099);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (1, 'PASSED', now(), 3.454);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (16, 'FAILED', now(), 0.137);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (17, 'PASSED', now(), 0.158);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (18, 'FAILED', now(), 0.15);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (19, 'FAILED', now(), 0.128);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (15, 'FAILED', now(), 1.375);
INSERT INTO public.test_item_results (result_id, status, end_time, duration)
VALUES (14, 'FAILED', now(), 1.644);


INSERT INTO public.item_attribute (key, value, item_id, launch_id, system)
VALUES ('skippedIssue', 'true', null, 1, true),
       ('testKey', 'testValue', null, 1, false),
       (null, 'tag', 1, null, false),
       (null, 'suite', 1, null, false),
       (null, 'tag', 2, null, false),
       (null, 'test', 2, null, false),
       (null, 'step', 3, null, false),
       (null, 'tag', 3, null, false),
       (null, 'finish', 3, null, false),
       (null, 'step', 4, null, false),
       (null, 'tag', 4, null, false),
       (null, 'finish', 4, null, false),
       (null, 'step', 5, null, false),
       ('browser', 'chrome', 5, null, false),
       (null, 'finish', 5, null, false),
       (null, 'tag', 5, null, false),
       ('browser', 'firefox', 6, null, false),
       (null, 'test', 6, null, false),
       (null, 'test', 7, null, false),
       ('browser', 'safari', 7, null, false),
       (null, 'finish', 7, null, false),
       ('step', 'supertest', 8, null, false),
       ('browser', 'chrome', 8, null, false),
       (null, 'test', 9, null, false),
       ('os', 'win', 9, null, false),
       ('os', 'mac', 10, null, false),
       (null, 'suite', 10, null, false),
       ('os', 'mac', 11, null, false),
       (null, 'suite', 11, null, false),
       (null, 'test', 12, null, false),
       ('os', 'win', 12, null, false),
       ('os', 'mac', 13, null, false),
       (null, 'test', 13, null, false),
       ('finish', 'passed', null, 1, false),
       (null, 'launch', null, 1, false),
       ('skippedIssue', 'true', null, 2, true),
       ('testKey', 'testValue', null, 2, false),
       ('sendBy', 'postman', null, 2, false),
       (null, 'suite', 14, null, false),
       (null, 'test', 15, null, false),
       (null, 'step', 16, null, false),
       (null, 'finish', 16, null, false),
       (null, 'step', 17, null, false),
       ('browser', 'chrome', 17, null, false),
       (null, 'finish', 17, null, false),
       (null, 'step', 18, null, false),
       ('browser', 'firefox', 18, null, false),
       (null, 'finish', 18, null, false),
       (null, 'step', 19, null, false),
       ('browser', 'safari', 19, null, false),
       (null, 'finish', 19, null, false),
       ('finish', 'failed', null, 2, false),
       (null, 'suite', null, 2, false),
       ('testKey', 'testValue', null, 3, false),
       ('testKey', 'testValue', null, 4, false);

INSERT INTO public.statistics_field (sf_id, name)
VALUES (1, 'statistics$executions$passed');
INSERT INTO public.statistics_field (sf_id, name)
VALUES (2, 'statistics$executions$total');
INSERT INTO public.statistics_field (sf_id, name)
VALUES (15, 'statistics$defects$product_bug$pb001');
INSERT INTO public.statistics_field (sf_id, name)
VALUES (16, 'statistics$defects$product_bug$total');
INSERT INTO public.statistics_field (sf_id, name)
VALUES (17, 'statistics$executions$failed');
INSERT INTO public.statistics_field (sf_id, name)
VALUES (21, 'statistics$defects$to_investigate$ti001');
INSERT INTO public.statistics_field (sf_id, name)
VALUES (22, 'statistics$defects$to_investigate$total');

INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (5, 1, null, 3, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (6, 1, null, 3, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (3, 2, null, 2, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (4, 2, null, 2, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (13, 1, null, 5, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (14, 1, null, 5, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (21, 1, null, 7, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (22, 1, null, 7, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (19, 2, null, 6, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (20, 2, null, 6, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (29, 1, null, 8, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (30, 1, null, 8, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (37, 1, null, 11, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (38, 1, null, 11, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (45, 1, null, 12, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (46, 1, null, 12, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (1, 7, null, 1, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (2, 7, null, 1, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (35, 3, null, 9, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (36, 3, null, 9, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (53, 1, null, 13, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (54, 1, null, 13, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (7, 7, 1, null, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (8, 7, 1, null, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (61, 1, null, 16, 15);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (62, 1, null, 16, 16);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (69, 1, null, 16, 17);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (70, 1, null, 16, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (73, 1, null, 14, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (75, 1, null, 15, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (77, 1, null, 17, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (78, 1, null, 17, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (79, 1, 2, null, 1);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (81, 1, null, 14, 21);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (82, 1, null, 14, 22);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (83, 1, null, 15, 21);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (84, 1, null, 15, 22);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (85, 1, null, 18, 21);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (86, 1, null, 18, 22);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (87, 1, 2, null, 21);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (88, 1, 2, null, 22);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (93, 1, null, 18, 17);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (94, 1, null, 18, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (57, 2, null, 14, 15);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (58, 2, null, 14, 16);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (59, 2, null, 15, 15);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (60, 2, null, 15, 16);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (101, 1, null, 19, 15);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (102, 1, null, 19, 16);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (63, 2, 2, null, 15);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (64, 2, 2, null, 16);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (65, 3, null, 14, 17);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (66, 4, null, 14, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (67, 3, null, 15, 17);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (68, 4, null, 15, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (109, 1, null, 19, 17);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (110, 1, null, 19, 2);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (71, 3, 2, null, 17);
INSERT INTO public.statistics (s_id, s_counter, launch_id, item_id, statistics_field_id)
VALUES (72, 4, 2, null, 2);

INSERT INTO public.issue (issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (16, 3, null, false, false);
INSERT INTO public.issue (issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (18, 1, null, false, false);
INSERT INTO public.issue (issue_id, issue_type, issue_description, auto_analyzed, ignore_analyzer)
VALUES (19, 3, null, false, false);

alter sequence launch_id_seq RESTART WITH 6;
alter sequence test_item_item_id_seq restart with 20;
alter sequence statistics_s_id_seq restart with 111;
alter sequence statistics_field_sf_id_seq restart with 23;