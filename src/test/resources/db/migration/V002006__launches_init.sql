-- Generates 12 launches in 'IN_PROGRESS' status on superadmin project and 1 launch with status 'FAILED'

CREATE OR REPLACE FUNCTION launches_init()
    RETURNS VOID AS
$$
DECLARE
    differentLaunchesCounter INT = 1; DECLARE sameLaunchCounter INT = 1;
BEGIN
    WHILE differentLaunchesCounter < 4
        LOOP
            raise notice 'Value: %', differentLaunchesCounter;
            WHILE sameLaunchCounter < 5
                LOOP
                    raise notice 'Value: %', sameLaunchCounter;
                    INSERT INTO public.launch (uuid, project_id, user_id, name, description,
                                               start_time, end_time, last_modified, mode, status)
                    VALUES ('uuid ' || differentLaunchesCounter || sameLaunchCounter,
                            1,
                            1,
                            'launch name ' || differentLaunchesCounter,
                            'description',
                            now() - make_interval(days := 14),
                            now() - make_interval(days := 14) + make_interval(mins := 1),
                            now() - make_interval(days := 14) + make_interval(mins := 1),
                            'DEFAULT',
                            'IN_PROGRESS');
                    sameLaunchCounter = sameLaunchCounter + 1;
                    IF sameLaunchCounter % 4 = 0
                    THEN
                        INSERT INTO item_attribute(key, value, system, launch_id)
                        VALUES ('key', 'value', true,
                                currval(pg_get_serial_sequence('launch', 'id')));
                    ELSE
                        INSERT INTO item_attribute(key, value, system, launch_id)
                        VALUES ('key', 'value', false,
                                currval(pg_get_serial_sequence('launch', 'id')));
                    END IF;
                END LOOP;
            sameLaunchCounter = 1;
            differentLaunchesCounter = differentLaunchesCounter + 1;
        END LOOP;
END;
$$
    LANGUAGE plpgsql;