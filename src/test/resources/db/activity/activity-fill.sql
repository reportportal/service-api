INSERT INTO launch (id, uuid, project_id, user_id, name, start_time, number, last_modified, mode, status)
VALUES (1, 'uuid', 2, 2, 'name', now(), 1, now(), 'DEFAULT', 'IN_PROGRESS');

INSERT INTO test_item (item_id, type, start_time, last_modified, launch_id)
VALUES (1, 'STEP', now(), now(), 1);

INSERT INTO activity(user_id, project_id, entity, action, details, creation_date, object_id) VALUES
(2, 2, 'DASHBOARD', 'dashboard_update', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": [
    {
      "field": "name",
      "newValue": "After Name",
      "oldValue": "Before Name"
    },
    {
      "field": "description",
      "newValue": "After Desc",
      "oldValue": "Before Desc"
    }
  ],
  "objectName": "name"
}', now() - INTERVAL '12 day', 1),

(2, 2, 'WIDGET', 'widget_create', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": null,
  "objectName": "widget test"
}', now() - interval '20 day', 1),

(2, 2, 'FILTER', 'filter_create', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": null,
  "objectName": "filter test"
}', now() - interval '3 day', 1),
(2, 2, 'FILTER', 'filter_create', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": [],
  "objectName": "filter new test"
}', now() - interval '2 day', 1),

(2, 2, 'FILTER', 'filter_update', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": [
    {
      "field": "name",
      "newValue": "filter new test",
      "oldValue": "filter test"
    },
    {
      "field": "description",
      "newValue": "new",
      "oldValue": "old"
    }
  ],
  "objectName": "filter new test"
}', now() - interval '1 day' - interval '4 hour', 2),

(2, 2, 'LAUNCH', 'start_launch', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": [],
  "objectName": "launch test"
}', now() - interval '2 day', 3),

(1, 1, 'LAUNCH', 'finish_launch', '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": [],
  "objectName": "launch test"
}', now() - interval '1 day', 4);