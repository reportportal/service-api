CREATE OR REPLACE FUNCTION clean_up_views()
    RETURNS VOID
AS
$$
DECLARE
    drop_query VARCHAR;
BEGIN
    drop_query := (SELECT 'DROP MATERIALIZED VIEW ' || string_agg(matviewname, ', ')
                   FROM pg_matviews
                   WHERE (matviewname LIKE 'widget_%'
                       OR matviewname LIKE 'hct_%')
                     AND matviewname
                       NOT IN
                         (SELECT widget_options -> 'options' ->> 'viewName'
                          FROM widget
                          WHERE widget_type IN ('componentHealthCheckTable', 'cumulative')
                            AND widget_options -> 'options' ->> 'viewName' NOTNULL));
    drop_query:= (SELECT CASE WHEN drop_query IS NOT NULL THEN drop_query ELSE 'select 1' end);
    EXECUTE drop_query;
END;
$$
    LANGUAGE plpgsql;

SELECT clean_up_views();
DROP FUNCTION IF EXISTS clean_up_views;