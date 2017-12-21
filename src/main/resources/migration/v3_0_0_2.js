db.serverSettings.update(
    {},
    {$rename: {"serverEmailConfig": "serverEmailDetails"}}
);

db.serverSettings.update(
    {_class: {$exists: true}},
    {
        $set: {"_class": "com.epam.ta.reportportal.database.entity.settings.ServerSettings"}
    }
);

db.serverSettings.serverEmailDetails.update(
    {_class: {$exists: true}},
    {
        $set: {"serverEmailDetails._class": "com.epam.ta.reportportal.database.entity.settings.ServerEmailDetails"}
    }
);
