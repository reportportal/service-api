INSERT INTO analytics_data (type, metadata)
VALUES ('ANALYZER_MANUAL_START',
        json_object(string_to_array('status,manually,version,5.11', ',')));
