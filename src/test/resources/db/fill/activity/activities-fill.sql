-- Inserts into activities table 7 records
insert into activity(id, action, event_name, created_at, details, object_id, object_name, object_type, priority,
 project_id, organization_id, subject_id, subject_name, subject_type)
values (1, 'UPDATE', 'updateDashboard', now() - interval '12 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
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
}', 1, 'name', 'DASHBOARD', 'LOW', 1, 1, 1, 'superadmin', 'USER'),

(2, 'CREATE', 'createWidget', now() - INTERVAL '20 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
  "history": null
}', 2, 'widget test', 'WIDGET', 'LOW', 1, 1, 1,'superadmin', 'USER'),

(3, 'CREATE', 'createFilter', now() - INTERVAL '3 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
  "history": null
}', 3, 'filter test', 'FILTER', 'LOW', 1, 1, 1, 'superadmin', 'USER'),

(4, 'CREATE', 'createFilter', now() - INTERVAL '2 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
  "history": []
}', 4, 'filter new test', 'FILTER', 'LOW', 2, 1, 2, 'user', 'USER'),

(5, 'UPDATE', 'updateFilter', now() - interval '1 day' - interval '4 hour', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
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
}', 4, 'filter new test', 'FILTER', 'LOW', 2, 1, 2, 'user', 'USER'),

(6, 'START', 'startLaunch', now() - interval '2 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
  "history": []
}', 5, 'launch test', 'LAUNCH', 'LOW', 2, 1, 2, 'user', 'USER'),

(7, 'FINISH', 'finishLaunch', now() - interval '1 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
  "history": []
}', 5, 'launch test', 'LAUNCH', 'LOW', 2, 1, 2, 'user', 'USER'),

(8, 'CREATE', 'createPlugin', now() - interval '1 day', '{
  "type": "com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails",
  "history": []
}', 5, 'nul org activity', 'PLUGIN', 'LOW', null, null, 2, 'user', 'USER');

alter sequence activity_id_seq restart with 9;
