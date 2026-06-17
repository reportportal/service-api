CREATE OR REPLACE FUNCTION merge_launch(launchid BIGINT)
    RETURNS INTEGER
AS
$$
DECLARE
    target_item_cursor CURSOR  (id BIGINT, lvl INT) FOR
        SELECT DISTINCT ON (unique_id) unique_id, item_id, path AS path_value, has_children
        FROM test_item parent
        WHERE parent.launch_id = id
          AND nlevel(parent.path) = lvl
          AND has_stats
          AND (parent.type = 'SUITE' OR (SELECT EXISTS(SELECT 1 FROM test_item t WHERE t.parent_id = parent.item_id LIMIT 1)));
    DECLARE
    merging_item_cursor CURSOR (uniqueid VARCHAR, lvl INT, launchid BIGINT) FOR
        SELECT item_id, path AS path_value, has_retries
        FROM test_item
        WHERE test_item.unique_id = uniqueid
          AND has_stats
          AND nlevel(test_item.path) = lvl
          AND test_item.launch_id = launchid;
    DECLARE
    target_item_field    RECORD;
    DECLARE
    merging_item_field   RECORD;
    DECLARE
    max_level            BIGINT;
    DECLARE
    first_item_unique_id VARCHAR;
    DECLARE
    item_has_children    BOOLEAN;
    DECLARE
    parent_item_id       BIGINT;
    DECLARE
    parent_item_path     LTREE;
    DECLARE
    concatenated_descr   TEXT;
BEGIN
    max_level := (SELECT MAX(nlevel(path))
                  FROM test_item
                  WHERE launch_id = launchid
                    AND has_stats);
    IF (max_level ISNULL)
    THEN
        RETURN 0;
    END IF;

    FOR i IN 1..max_level
        LOOP

            OPEN target_item_cursor(launchid, i);

            LOOP
                FETCH target_item_cursor INTO target_item_field;

                EXIT WHEN NOT found;

                first_item_unique_id := target_item_field.unique_id;
                parent_item_id := target_item_field.item_id;
                parent_item_path := target_item_field.path_value;
                item_has_children := target_item_field.has_children;

                EXIT WHEN first_item_unique_id ISNULL;

                IF item_has_children
                THEN
                    SELECT string_agg(description, chr(10))
                    INTO concatenated_descr
                    FROM test_item
                    WHERE test_item.unique_id = first_item_unique_id
                      AND has_stats
                      AND has_children
                      AND nlevel(test_item.path) = i
                      AND test_item.launch_id = launchid;

                    UPDATE test_item SET description = concatenated_descr WHERE test_item.item_id = parent_item_id;

                    UPDATE test_item
                    SET start_time = (SELECT min(start_time)
                                      FROM test_item
                                      WHERE test_item.unique_id = first_item_unique_id
                                        AND has_stats
                                        AND has_children
                                        AND nlevel(test_item.path) = i
                                        AND test_item.launch_id = launchid)
                    WHERE test_item.item_id = parent_item_id;

                    UPDATE test_item_results
                    SET end_time = (SELECT max(end_time)
                                    FROM test_item
                                             JOIN test_item_results result ON test_item.item_id = result.result_id
                                    WHERE test_item.unique_id = first_item_unique_id
                                      AND has_stats
                                      AND has_children
                                      AND nlevel(test_item.path) = i
                                      AND test_item.launch_id = launchid)
                    WHERE test_item_results.result_id = parent_item_id;

                    INSERT INTO statistics (statistics_field_id, item_id, launch_id, s_counter)
                    SELECT statistics_field_id, parent_item_id, NULL, sum(s_counter)
                    FROM statistics
                             JOIN test_item ti ON statistics.item_id = ti.item_id
                    WHERE ti.unique_id = first_item_unique_id
                      AND ti.launch_id = launchid
                      AND nlevel(ti.path) = i
                      AND ti.has_stats
                      AND ti.has_children
                    GROUP BY statistics_field_id
                    ON CONFLICT ON CONSTRAINT unique_stats_item DO UPDATE
                        SET s_counter = excluded.s_counter;

                    IF exists(SELECT 1
                              FROM test_item_results
                                       JOIN test_item t ON test_item_results.result_id = t.item_id
                              WHERE (test_item_results.status != 'PASSED' AND test_item_results.status != 'SKIPPED')
                                AND t.unique_id = first_item_unique_id
                                AND nlevel(t.path) = i
                                AND t.has_stats
                                AND t.has_children
                                AND t.launch_id = launchid)
                    THEN
                        UPDATE test_item_results SET status = 'FAILED' WHERE test_item_results.result_id = parent_item_id;
                    ELSEIF exists(SELECT 1
                                  FROM test_item_results
                                           JOIN test_item t ON test_item_results.result_id = t.item_id
                                  WHERE test_item_results.status != 'PASSED'
                                    AND t.unique_id = first_item_unique_id
                                    AND nlevel(t.path) = i
                                    AND t.has_stats
                                    AND t.has_children
                                    AND t.launch_id = launchid)
                    THEN
                        UPDATE test_item_results SET status = 'SKIPPED' WHERE test_item_results.result_id = parent_item_id;
                    ELSE
                        UPDATE test_item_results SET status = 'PASSED' WHERE test_item_results.result_id = parent_item_id;
                    END IF;
                END IF;


                OPEN merging_item_cursor(target_item_field.unique_id, i, launchid);

                LOOP

                    FETCH merging_item_cursor INTO merging_item_field;

                    EXIT WHEN NOT found;

                    IF (SELECT EXISTS(SELECT 1
                                      FROM test_item t
                                      WHERE (t.parent_id = merging_item_field.item_id
                                          OR (t.item_id = merging_item_field.item_id AND t.type = 'SUITE'))
                                        AND t.has_stats))
                    THEN
                        UPDATE test_item
                        SET parent_id = parent_item_id,
                            path      = text2ltree(concat(parent_item_path :: TEXT, '.', test_item.item_id :: TEXT))
                        WHERE test_item.parent_id = merging_item_field.item_id
                          AND nlevel(test_item.path) = i + 1
                          AND has_stats
                          AND test_item.retry_of IS NULL;
                        DELETE
                        FROM test_item
                        WHERE test_item.path = merging_item_field.path_value
                          AND test_item.has_stats
                          AND test_item.item_id != parent_item_id;

                    END IF;

                    IF merging_item_field.has_retries
                    THEN
                        UPDATE test_item
                        SET path = text2ltree(concat(merging_item_field.path_value :: TEXT, '.', test_item.item_id :: TEXT))
                        WHERE test_item.retry_of = merging_item_field.item_id;
                    END IF;

                END LOOP;

                CLOSE merging_item_cursor;

            END LOOP;

            CLOSE target_item_cursor;

        END LOOP;


    INSERT INTO statistics (statistics_field_id, launch_id, s_counter)
    SELECT statistics_field_id, launchid, sum(s_counter)
    FROM statistics
             JOIN test_item ti ON statistics.item_id = ti.item_id
    WHERE ti.launch_id = launchid
      AND ti.has_stats
      AND ti.parent_id IS NULL
    GROUP BY statistics_field_id
    ON CONFLICT ON CONSTRAINT unique_stats_launch DO UPDATE
        SET s_counter = excluded.s_counter;

    RETURN 0;
END;
$$
    LANGUAGE plpgsql;