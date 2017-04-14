db.serverSettings.serverEmailDetails.update(
    {
        $unset: {"serverEmailDetails.debug": ""}
    }
)