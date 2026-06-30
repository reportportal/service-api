ALTER TYPE status_enum RENAME TO status_enum_old;

CREATE TYPE status_enum AS ENUM ('CANCELLED', 'FAILED', 'INTERRUPTED', 'IN_PROGRESS', 'PASSED', 'RESETED', 'SKIPPED', 'STOPPED', 'INFO', 'WARN');

ALTER TABLE launch
    ALTER COLUMN status TYPE status_enum USING status::text::status_enum;
ALTER TABLE test_item_results
    ALTER COLUMN status TYPE status_enum USING status::text::status_enum;

DROP TYPE status_enum_old;

CREATE OR REPLACE FUNCTION update_executions_statistics()
    RETURNS TRIGGER AS
$$
DECLARE
    DECLARE
    executions_field          VARCHAR;
    DECLARE
    executions_field_id       BIGINT;
    DECLARE
    executions_field_old      VARCHAR;
    DECLARE
    executions_field_old_id   BIGINT;
    DECLARE
    executions_field_total    VARCHAR;
    DECLARE
    executions_field_total_id BIGINT;
    DECLARE
    cur_launch_id             BIGINT;
    DECLARE
    counter_decrease          INTEGER;

BEGIN
    IF exists(SELECT 1
              FROM test_item
              WHERE (test_item.parent_id = new.result_id
                  AND test_item.has_stats)
                 OR (test_item.item_id = new.result_id AND (NOT test_item.has_stats OR
                                                            (test_item.type != 'TEST' :: TEST_ITEM_TYPE_ENUM AND
                                                             test_item.type != 'SCENARIO' :: TEST_ITEM_TYPE_ENUM AND
                                                             test_item.type != 'STEP' :: TEST_ITEM_TYPE_ENUM)))
              LIMIT 1)
    THEN
        RETURN new;
    END IF;

    IF exists(SELECT 1
              FROM test_item
              WHERE item_id = new.result_id
                AND retry_of IS NOT NULL
              LIMIT 1)
    THEN
        RETURN new;
    END IF;

    cur_launch_id := (SELECT launch_id FROM test_item WHERE test_item.item_id = new.result_id);

    IF cur_launch_id IS NULL
    THEN
        RETURN new;
    END IF;

    IF new.status = 'INTERRUPTED' :: STATUS_ENUM
    THEN
        executions_field := 'statistics$executions$failed';
    ELSE
        executions_field := concat('statistics$executions$', lower(new.status :: VARCHAR));
    END IF;

    executions_field_total := 'statistics$executions$total';

    INSERT INTO statistics_field (name) VALUES (executions_field) ON CONFLICT DO NOTHING;

    INSERT INTO statistics_field (name) VALUES (executions_field_total) ON CONFLICT DO NOTHING;

    executions_field_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                           FROM statistics_field
                           WHERE statistics_field.name = executions_field);
    executions_field_total_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                                 FROM statistics_field
                                 WHERE statistics_field.name = executions_field_total);

    IF old.status = 'IN_PROGRESS' :: STATUS_ENUM
    THEN
        INSERT INTO statistics (s_counter, statistics_field_id, item_id)
            (SELECT 1, executions_field_id, item_id
             FROM (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.result_id)) AS temp_bulk)
        ON CONFLICT (statistics_field_id,
            item_id)
            DO UPDATE SET s_counter = statistics.s_counter + 1;

        INSERT INTO statistics (s_counter, statistics_field_id, item_id)
            (SELECT 1, executions_field_total_id, item_id
             FROM (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.result_id)) AS temp_bulk)
        ON CONFLICT (statistics_field_id,
            item_id)
            DO UPDATE SET s_counter = statistics.s_counter + 1;

        /* increment launch executions statistics for concrete field */
        INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
        VALUES (1, executions_field_id, cur_launch_id)
        ON CONFLICT (statistics_field_id,
            launch_id)
            DO UPDATE SET s_counter = statistics.s_counter + 1;
        /* increment launch executions statistics for total field */
        INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
        VALUES (1, executions_field_total_id, cur_launch_id)
        ON CONFLICT (statistics_field_id,
            launch_id)
            DO UPDATE SET s_counter = statistics.s_counter + 1;
        RETURN new;
    END IF;

    IF old.status != 'IN_PROGRESS' :: STATUS_ENUM AND old.status != new.status
    THEN
        IF old.status = 'INTERRUPTED' :: STATUS_ENUM
        THEN
            executions_field_old := 'statistics$executions$failed';
        ELSE
            executions_field_old := concat('statistics$executions$', lower(old.status :: VARCHAR));
        END IF;

        executions_field_old_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                                   FROM statistics_field
                                   WHERE statistics_field.name = executions_field_old);

        SELECT s_counter
        INTO counter_decrease
        FROM statistics
        WHERE item_id = new.result_id
          AND statistics_field_id = executions_field_old_id;

        /* decrease item executions statistics for old field */
        UPDATE statistics
        SET s_counter = s_counter - counter_decrease
        WHERE statistics_field_id = executions_field_old_id
          AND item_id IN (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.result_id));

        /* increment item executions statistics for concrete field */
        INSERT INTO statistics (s_counter, statistics_field_id, item_id)
            (SELECT 1, executions_field_id, item_id
             FROM (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.result_id)) AS temp_bulk)
        ON CONFLICT (statistics_field_id,
            item_id)
            DO UPDATE SET s_counter = statistics.s_counter + 1;


        /* decrease item executions statistics for old field */
        UPDATE statistics
        SET s_counter = s_counter - counter_decrease
        WHERE statistics_field_id = executions_field_old_id
          AND launch_id = cur_launch_id;
        /* increment launch executions statistics for concrete field */
        INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
        VALUES (1, executions_field_id, cur_launch_id)
        ON CONFLICT (statistics_field_id,
            launch_id)
            DO UPDATE SET s_counter = statistics.s_counter + 1;
        RETURN new;
    END IF;
    RETURN new;
END;
$$
    LANGUAGE plpgsql;