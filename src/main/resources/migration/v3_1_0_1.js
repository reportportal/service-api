db.serverSettings.update(
    {},
    {$unset: {"serverEmailDetails.debug": ""}}
);
