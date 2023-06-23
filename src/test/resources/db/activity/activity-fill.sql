insert into launch (id, uuid, project_id, user_id, name, start_time, number, last_modified, mode,
                    status)
values (1, 'uuid', 2, 2, 'name', now(), 1, now(), 'DEFAULT', 'IN_PROGRESS');

insert into test_item (test_case_hash, item_id, uuid, type, start_time, last_modified, launch_id)
values (1, 1, 'uuid', 'STEP', now(), now(), 1);

insert into activity(created_at, action, event_name, priority, object_id, object_name, object_type, project_id, details, subject_id, subject_name, subject_type)
values (now() - interval '12 day', 'UPDATE', 'updateDashboard', 'MEDIUM', 1, 'name', 'DASHBOARD', 2, '{
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
  ]
}', 2, 'user', 'USER'),

       (now() - INTERVAL '20 day', 'CREATE', 'createWidget', 'MEDIUM', 1, 'widget test', 'WIDGET', 2, '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": null
}', 2, 'user', 'USER'),

       (now() - INTERVAL '3 day', 'CREATE', 'createFilter', 'MEDIUM', 1, 'filter test', 'FILTER', 2, '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": null
}', 2, 'user', 'USER'),

       (now() - INTERVAL '2 day', 'CREATE', 'createFilter', 'MEDIUM', 1, 'filter new test', 'FILTER', 2, '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": null
}', 2, 'user', 'USER'),

       (now() - INTERVAL '1 day' - INTERVAL '4 hour', 'UPDATE', 'updateFilter', 'MEDIUM', 2, 'filter new test', 'FILTER', 2, '{
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
  ]
}', 2, 'user', 'USER'),

       (now() - INTERVAL '2 day', 'START', 'startLaunch', 'MEDIUM', 3, 'launch test', 'LAUNCH', 2, '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": []
}', 2, 'user', 'USER'),

       (now() - INTERVAL '1 day', 'FINISH', 'finishLaunch', 'MEDIUM', 4, 'launch test', 'LAUNCH', 1, '{
  "type": "com.epam.ta.reportportal.entity.activity.ActivityDetails",
  "history": []
}', 1, 'superadmin', 'USER');