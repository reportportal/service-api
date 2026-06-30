CREATE OR REPLACE FUNCTION handle_retry(retry_id BIGINT, retry_parent_id BIGINT)
    RETURNS INTEGER
AS
$$
DECLARE
    current_retry_id BIGINT;
BEGIN

    IF retry_id IS NULL OR retry_parent_id IS NULL
    THEN
        RETURN 1;
    END IF;

    current_retry_id := (SELECT retry_of FROM test_item WHERE item_id = retry_id FOR UPDATE);

    IF (current_retry_id IS NOT NULL)
    THEN
        retry_id := (SELECT item_id FROM test_item WHERE item_id = current_retry_id FOR UPDATE);
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

