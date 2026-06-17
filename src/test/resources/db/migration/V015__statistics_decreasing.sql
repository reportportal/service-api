CREATE OR REPLACE FUNCTION decrease_statistics()
    RETURNS TRIGGER AS
$$
DECLARE
    cur_launch_id         BIGINT;
    DECLARE
    cur_id                BIGINT;
    DECLARE
    cur_statistics_fields RECORD;
BEGIN

    IF exists(SELECT 1
              FROM test_item
              WHERE item_id = old.result_id
                AND NOT has_stats
              LIMIT 1)
    THEN
        RETURN old;
    END IF;

    cur_launch_id := (SELECT launch_id FROM test_item WHERE item_id = old.result_id);

    IF cur_launch_id IS NULL
    THEN
        RETURN old;
    END IF;

    IF exists(SELECT 1
              FROM test_item
              WHERE item_id = old.result_id
                AND retry_of IS NOT NULL
              LIMIT 1)
    THEN
        RETURN old;
    END IF;

    FOR cur_statistics_fields IN (SELECT statistics_field_id, s_counter FROM statistics WHERE item_id = old.result_id)
        LOOP
            UPDATE statistics
            SET s_counter = s_counter - cur_statistics_fields.s_counter
            WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
              AND launch_id = cur_launch_id;
        END LOOP;

    FOR cur_id IN
        (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = old.result_id) ORDER BY item_id)

        LOOP
            FOR cur_statistics_fields IN (SELECT statistics_field_id, s_counter FROM statistics WHERE item_id = old.result_id)
                LOOP
                    UPDATE statistics
                    SET s_counter = s_counter - cur_statistics_fields.s_counter
                    WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
                      AND item_id = cur_id;
                END LOOP;
        END LOOP;

    RETURN old;
END;
$$
    LANGUAGE plpgsql;