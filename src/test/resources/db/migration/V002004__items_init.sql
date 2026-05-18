-- Generates 13 launches. Each contains suite, test and 4 step items with testItem results. Launches and items has attributes

CREATE OR REPLACE FUNCTION items_init()
    RETURNS VOID AS
$$
DECLARE
    launchcounter          INT = 1;
    DECLARE retriescounter INT = 1;
    DECLARE cur_suite_id   BIGINT;
    DECLARE cur_item_id    BIGINT;
    DECLARE cur_step_id    BIGINT;
    DECLARE stepcounter    INT = 1;
    DECLARE functionresult INT = 0;
BEGIN

    alter sequence launch_id_seq
        restart with 1;
    alter sequence attribute_id_seq
        restart with 1;
    alter sequence test_item_item_id_seq
        restart with 1;
    alter sequence log_id_seq
        restart with 1;
    alter sequence pattern_template_id_seq
        restart with 1;

    INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
    VALUES (1, 'name1', 'qwe', 'STRING', true, 1);
    INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
    VALUES (2, 'name2', 'qw', 'STRING', true, 1);
    INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
    VALUES (3, 'name3', 'qwee', 'STRING', false, 1);
    INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
    VALUES (4, 'name4', '[a-z]{2,4}', 'REGEX', false, 1);
    INSERT INTO pattern_template (id, name, value, type, enabled, project_id)
    VALUES (5, 'name5_p2', '^*+', 'REGEX', true, 2);

    WHILE launchcounter < 13
        LOOP
            INSERT INTO launch (id, uuid, project_id, user_id, name, start_time, number,
                                last_modified, mode, status)
            VALUES (launchcounter, 'uuid ' || launchcounter, 1, 1, 'name ' || launchcounter, now(),
                    1, now(), 'DEFAULT', 'IN_PROGRESS');

            INSERT INTO item_attribute (key, value, item_id, launch_id, system)
            VALUES ('key' || launchcounter % 4, 'value' || launchcounter, NULL, launchcounter,
                    FALSE);

            IF floor(random() * (3 - 1 + 1) + 1) = 2
            THEN
                INSERT INTO item_attribute (key, value, item_id, launch_id, system)
                VALUES ('systemKey', 'systemValue', NULL, launchcounter, TRUE);
            END IF;

            launchcounter = launchcounter + 1;
        END LOOP;

    launchcounter = 1;

    WHILE launchcounter < 13
        LOOP
            INSERT INTO test_item (test_case_hash, has_children, name, uuid, type, start_time,
                                   description, last_modified, unique_id,
                                   launch_id)
            VALUES (1, true, 'SUITE ' || launchcounter, 'uuid 1_' || launchcounter, 'SUITE', now(),
                    'description', now(),
                    'unqIdSUITE' || launchcounter, launchcounter);
            cur_suite_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

            INSERT INTO item_attribute (key, value, item_id, launch_id, system)
            VALUES ('suite', 'value' || cur_suite_id, cur_suite_id, NULL, FALSE);

            UPDATE test_item
            SET path = cast(cast(cur_suite_id AS TEXT) AS LTREE)
            WHERE item_id = cur_suite_id;

            INSERT INTO test_item_results (result_id, status, duration, end_time)
            VALUES (cur_suite_id, 'FAILED', 0.35, now());
            --
            INSERT INTO test_item (test_case_hash, has_children, name, uuid, type, start_time,
                                   description, last_modified, unique_id,
                                   launch_id, parent_id)
            VALUES (2, true, 'uuid 2_' || launchcounter, 'First test' || launchcounter, 'TEST',
                    now(), 'description', now(),
                    'unqIdTEST' || launchcounter,
                    launchcounter, cur_suite_id);
            cur_item_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

            INSERT INTO item_attribute (key, value, item_id, launch_id, system)
            VALUES ('test', 'value' || cur_item_id, cur_item_id, NULL, FALSE);

            UPDATE test_item
            SET path = cast(cur_suite_id AS TEXT) || cast(cast(cur_item_id AS TEXT) AS LTREE)
            WHERE item_id = cur_item_id;

            INSERT INTO test_item_results (result_id, status, duration, end_time)
            VALUES (cur_item_id, 'FAILED', 0.35, now());

            WHILE stepcounter < 8
                LOOP
                    --

                    IF launchcounter = 1 AND stepcounter > 3
                    THEN
                        launchcounter = launchcounter + 1;
                        CONTINUE;
                    END IF;

                    INSERT INTO test_item (test_case_hash, name, code_ref, uuid, type, start_time,
                                           description, last_modified, unique_id,
                                           parent_id, launch_id)
                    VALUES (3, 'Step', 'package.Classname', 'uuid 3_' || launchcounter ||
                                                            stepcounter, 'STEP', now(),
                            'description', now(),
                            'unqIdSTEP' || launchcounter,
                            cur_item_id, launchcounter);
                    cur_step_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

                    IF stepcounter % 2 = 0
                    THEN
                        INSERT INTO item_attribute (key, value, item_id, launch_id, system)
                        VALUES ('step', 'value' || cur_step_id, cur_step_id, NULL, TRUE);

                        INSERT INTO item_attribute (key, value, item_id, launch_id, system)
                        VALUES ('systemTestKey', 'systemTestValue', cur_step_id, NULL, TRUE);

                    ELSE
                        INSERT INTO item_attribute (key, value, item_id, launch_id, system)
                        VALUES ('step', 'value' || cur_step_id, cur_step_id, NULL, FALSE);
                        INSERT INTO item_attribute (key, value, item_id, launch_id, system)
                        VALUES (null, 'value' || cur_step_id, cur_step_id, NULL, FALSE);
                    END IF;

                    IF cur_step_id = 3
                    THEN
                        PERFORM logs_init();
                    END IF;

                    UPDATE test_item
                    SET path = cast(cur_suite_id AS TEXT) ||
                               cast(cast(cur_item_id AS TEXT) AS LTREE) || cast(cur_step_id AS TEXT)
                    WHERE item_id = cur_step_id;

                    INSERT INTO test_item_results (result_id, status, duration, end_time)
                    VALUES (cur_step_id, 'IN_PROGRESS', 0.35, now());

                    IF stepcounter = 1
                    THEN
                        UPDATE test_item_results
                        SET status = 'FAILED'
                        WHERE result_id = cur_step_id;
                        INSERT INTO issue (issue_id, issue_type, auto_analyzed, issue_description)
                        VALUES (cur_step_id, 2, FALSE, 'issue description');

                        INSERT INTO ticket (ticket_id, submitter, submit_date, bts_url, bts_project,
                                            url)
                        VALUES (concat('ticket_id_', cur_step_id), 'superadmin', now(), 'jira.com',
                                'project',
                                concat('http://example.com/tickets/ticket_id_', cur_step_id));
                        INSERT INTO issue_ticket (issue_id, ticket_id)
                        VALUES (cur_step_id,
                                (SELECT currval(pg_get_serial_sequence('ticket', 'id'))));

                        INSERT INTO pattern_template_test_item (pattern_id, item_id)
                        VALUES (1, cur_step_id);
                    END IF;

                    IF stepcounter = 2
                    THEN
                        UPDATE test_item
                        SET last_modified = '2018-11-08 12:00:00'
                        WHERE item_id = cur_step_id;
                        INSERT INTO pattern_template_test_item (pattern_id, item_id)
                        VALUES (2, cur_step_id);
                        INSERT INTO pattern_template_test_item (pattern_id, item_id)
                        VALUES (3, cur_step_id);
                    END IF;

                    IF stepcounter = 3
                    THEN
                        UPDATE test_item
                        SET last_modified = now() - make_interval(days := 14)
                        WHERE item_id = cur_step_id;
                        INSERT INTO pattern_template_test_item (pattern_id, item_id)
                        VALUES (3, cur_step_id);
                    END IF;
                    stepcounter = stepcounter + 1;
                END LOOP;
            stepcounter = 1;

            IF stepcounter > 3
            THEN
                UPDATE test_item_results SET status = 'FAILED' WHERE result_id = cur_step_id;
                INSERT INTO issue (issue_id, issue_type, auto_analyzed, issue_description)
                VALUES (cur_step_id, floor(random() * 4 + 2), FALSE, 'issue description');
            END IF;

            launchcounter = launchcounter + 1;
        END LOOP;

    -- RETRIES --

    INSERT INTO test_item (test_case_hash, name, uuid, type, start_time, description, last_modified,
                           unique_id, launch_id)
    VALUES (4, 'SUITE ' || launchcounter - 1,
            'uuid 4_' || launchcounter,
            'SUITE',
            now(),
            'suite with retries',
            now(),
            'unqIdSUITE_R' || launchcounter - 1,
            launchcounter - 1);
    cur_suite_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

    UPDATE test_item
    SET path = cast(cast(cur_suite_id AS TEXT) AS LTREE)
    WHERE item_id = cur_suite_id;

    INSERT INTO test_item_results (result_id, status, duration, end_time)
    VALUES (cur_suite_id, 'FAILED', 0.35, now());
    --
    INSERT INTO test_item (test_case_hash, name, uuid, type, start_time, description, last_modified,
                           unique_id, launch_id, parent_id)
    VALUES (5, 'First test', 'uuid 5_' || launchcounter, 'TEST', now(), 'test with retries', now(),
            'unqIdTEST_R' || launchcounter - 1,
            launchcounter - 1, cur_suite_id);
    cur_item_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

    UPDATE test_item
    SET path = cast(cur_suite_id AS TEXT) || cast(cast(cur_item_id AS TEXT) AS LTREE)
    WHERE item_id = cur_item_id;

    INSERT INTO test_item_results (result_id, status, duration, end_time)
    VALUES (cur_item_id, 'FAILED', 0.35, now());

    INSERT INTO test_item (test_case_hash, name, uuid, type, start_time, description, last_modified,
                           unique_id, parent_id, launch_id)
    VALUES (6, 'Step', 'another uuid 6_' || launchcounter, 'STEP', now(), 'STEP WITH RETRIES',
            now(), 'unqIdSTEP_R' || launchcounter - 1,
            cur_item_id,
            launchcounter - 1);
    cur_step_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

    UPDATE test_item
    SET path = cast(cur_suite_id AS TEXT) || cast(cast(cur_item_id AS TEXT) AS LTREE) ||
               cast(cur_step_id AS TEXT)
    WHERE item_id = cur_step_id;

    INSERT INTO test_item_results (result_id, status, duration, end_time)
    VALUES (cur_step_id, 'IN_PROGRESS', 0.35, now());

    WHILE retriescounter < 4
        LOOP

            INSERT INTO test_item (test_case_hash, name, uuid, type, start_time, description,
                                   last_modified, unique_id, parent_id,
                                   launch_id)
            VALUES (7, 'Step',
                    'uuid 7_' || launchcounter || retriescounter,
                    'STEP',
                    now() - make_interval(secs := retriescounter),
                    'STEP WITH RETRIES',
                    now() - make_interval(secs := retriescounter),
                    'unqIdSTEP_R' || launchcounter - 1,
                    cur_item_id,
                    launchcounter - 1);
            cur_step_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));

            UPDATE test_item
            SET path = cast(cur_suite_id AS TEXT) || cast(cast(cur_item_id AS TEXT) AS LTREE) ||
                       cast(cur_step_id AS TEXT)
            WHERE item_id = cur_step_id;

            INSERT INTO test_item_results (result_id, status, duration, end_time)
            VALUES (cur_step_id, 'IN_PROGRESS', 0.35, now());

            INSERT INTO parameter (item_id, key, value)
            VALUES (cur_step_id, 'first key', 'first value');
            INSERT INTO parameter (item_id, key, value)
            VALUES (cur_step_id, 'second key', 'second value');
            INSERT INTO parameter (item_id, key, value)
            VALUES (cur_step_id, 'third key', 'third value');

            functionresult := (SELECT handle_retries(cur_step_id));

            retriescounter = retriescounter + 1;

        END LOOP;

    functionresult := (SELECT retries_statistics(launchcounter - 1));

    INSERT INTO test_item (test_case_hash, name, uuid, type, start_time, description, last_modified,
                           unique_id, parent_id, launch_id)
    VALUES (8, 'Step', 'some uuid 6_' || launchcounter, 'STEP', now(), 'Descendant', now(),
            'unqIdSTEP_R' || launchcounter - 1, 5, 1);
    cur_step_id = (SELECT currval(pg_get_serial_sequence('test_item', 'item_id')));
    UPDATE test_item
    SET path = cast('1.2.5.' || cast(cur_step_id AS TEXT) AS LTREE)
    WHERE item_id = cur_step_id;
    UPDATE test_item SET has_children = true WHERE item_id = 5;
    UPDATE test_item_results SET status = 'FAILED' WHERE result_id = 5;
    INSERT INTO test_item_results (result_id, status, duration, end_time)
    VALUES ((SELECT currval(pg_get_serial_sequence('test_item', 'item_id'))), 'FAILED', 0.35,
            now());
END;
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION logs_init()
    RETURNS VOID AS
$$
DECLARE
    stepid                INT = 3;
    DECLARE logscounter   INT = 1;
    DECLARE launchcounter INT = 1;
BEGIN
    WHILE logscounter < 4
        LOOP

            INSERT INTO attachment (file_id, thumbnail_id, content_type, file_size, project_id,
                                    launch_id, item_id, creation_date)
            VALUES ('attach ' || logscounter, 'attachThumb' || logscounter, 'MIME', 1024, 1, 1,
                    stepid, now());

            INSERT INTO log (log_time, uuid, log_message, item_id, last_modified, log_level,
                             attachment_id, project_id)
            VALUES (now() - make_interval(days := 14), 'uuid' || logscounter, 'log', stepid,
                    now() - make_interval(days := 14), 40000,
                    (SELECT currval(pg_get_serial_sequence('attachment', 'id'))), 1);

            logscounter = logscounter + 1;
        END LOOP;

    WHILE logscounter > 0
        LOOP

            INSERT INTO attachment (file_id, thumbnail_id, content_type, file_size, project_id,
                                    launch_id, item_id, creation_date)
            VALUES ('attach ' || logscounter, 'attachThumb' || logscounter, 'MIME', 1024, 1, 1,
                    stepid, now());

            INSERT INTO log (uuid, log_time, log_message, item_id, last_modified, log_level,
                             attachment_id, project_id)
            VALUES ('luuid' || logscounter, now(), 'log', stepid, now(), 40000,
                    (SELECT currval(pg_get_serial_sequence('attachment', 'id'))), 1);

            logscounter = logscounter - 1;
        END LOOP;

    WHILE launchcounter < 7
        LOOP
            INSERT INTO attachment (file_id, thumbnail_id, content_type, project_id, file_size,
                                    launch_id, item_id, creation_date)
            VALUES ('attach_log ' || launchcounter, 'attachThumb_log ' || launchcounter, 'MIME', 1,
                    1024, launchcounter, null, now());

            INSERT INTO log (uuid, log_time, log_message, launch_id, last_modified, log_level,
                             attachment_id, project_id)
            VALUES ('lluuid' || launchcounter, now() - make_interval(days := 14), 'log',
                    launchcounter, now() - make_interval(days := 14),
                    40000,
                    (SELECT currval(pg_get_serial_sequence('attachment', 'id'))), 1);
            launchcounter = launchcounter + 1;
        END LOOP;
END;
$$
    LANGUAGE plpgsql;
