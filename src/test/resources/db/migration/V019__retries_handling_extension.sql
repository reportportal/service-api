CREATE OR REPLACE FUNCTION delete_item_statistics(test_item_id BIGINT)
    RETURNS INTEGER AS
$$
DECLARE
    cur_id                BIGINT;
    cur_launch_id         BIGINT;
    cur_statistics_fields RECORD;
BEGIN

    IF
        test_item_id IS NULL
    THEN
        RETURN 1;
    END IF;

    cur_launch_id := (SELECT test_item.launch_id FROM test_item WHERE test_item.item_id = test_item_id);

    IF
        cur_launch_id IS NULL
    THEN
        RETURN 1;
    END IF;

    DELETE FROM issue WHERE issue_id = test_item_id;

    FOR cur_statistics_fields IN (SELECT statistics_field_id, sum(s_counter) AS counter_sum
                                  FROM statistics
                                           JOIN test_item ti ON statistics.item_id = ti.item_id
                                  WHERE ti.item_id = test_item_id
                                  GROUP BY statistics_field_id)
        LOOP
            UPDATE statistics
            SET s_counter = s_counter - cur_statistics_fields.counter_sum
            WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
              AND launch_id = cur_launch_id;
        END LOOP;

    FOR cur_id IN
        (SELECT item_id
         FROM test_item
         WHERE path @> (SELECT path FROM test_item WHERE item_id = test_item_id)
           AND item_id != test_item_id)

        LOOP
            FOR cur_statistics_fields IN (SELECT statistics_field_id, sum(s_counter) AS counter_sum
                                          FROM statistics
                                                   JOIN test_item ti ON statistics.item_id = ti.item_id
                                          WHERE ti.item_id = test_item_id
                                          GROUP BY statistics_field_id)
                LOOP
                    UPDATE statistics
                    SET s_counter = s_counter - cur_statistics_fields.counter_sum
                    WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
                      AND item_id = cur_id;
                END LOOP;
        END LOOP;

    DELETE FROM statistics WHERE item_id = test_item_id;

    RETURN 0;
END;
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION handle_retry(retry_id BIGINT, retry_parent_id BIGINT)
    RETURNS INTEGER
AS
$$
BEGIN

    IF retry_id IS NULL OR retry_parent_id IS NULL
    THEN
        RETURN 1;
    END IF;

    PERFORM delete_item_statistics(retry_id);

    UPDATE test_item
    SET retry_of    = retry_parent_id,
        launch_id   = NULL,
        has_retries = FALSE,
        path        = ((SELECT path FROM test_item WHERE item_id = retry_parent_id) :: TEXT || '.' || item_id) :: LTREE
    WHERE retry_of = retry_id;

    UPDATE test_item
    SET retry_of    = retry_parent_id,
        launch_id   = NULL,
        has_retries = FALSE,
        path        = ((SELECT path FROM test_item WHERE item_id = retry_parent_id) :: TEXT || '.' || item_id) :: LTREE
    WHERE item_id = retry_id;

    UPDATE test_item
    SET has_retries = TRUE
    WHERE item_id = retry_parent_id;

    RETURN 0;
END;
$$
    LANGUAGE plpgsql;