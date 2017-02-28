db.serverSettings.update(
    {},
    {$rename: {"serverEmailConfig":"serverEmailDetails"}})
db.serverSettings.update(
    {},
    {
        $set: {"_class": "com.epam.ta.reportportal.database.entity.settings.ServerSettings",
            "serverEmailDetails._class": "com.epam.ta.reportportal.database.entity.settings.ServerEmailDetails"}
    }
)