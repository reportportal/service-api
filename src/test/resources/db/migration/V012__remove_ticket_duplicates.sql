CREATE INDEX IF NOT EXISTS ticket_id_idx ON ticket(ticket_id);
CREATE OR REPLACE FUNCTION migrate_tickets() RETURNS VOID AS
$$
DECLARE
    duplicated_ticket RECORD;
    index             BIGINT;
BEGIN
    FOR duplicated_ticket IN SELECT min(id) AS pid, ticket_id AS tid
                             FROM ticket
                             GROUP BY ticket_id, bts_project, url
                             HAVING array_length(array_agg(id), 1) > 1
        LOOP
            FOR index IN SELECT id FROM ticket WHERE ticket_id = duplicated_ticket.tid AND id != duplicated_ticket.pid
                LOOP
                    UPDATE issue_ticket SET ticket_id = duplicated_ticket.pid WHERE ticket_id = index;
                    DELETE FROM ticket WHERE id = index;
                END LOOP;
        END LOOP;
END;
$$
    LANGUAGE plpgsql;
SELECT migrate_tickets();
DROP FUNCTION IF EXISTS migrate_tickets();
