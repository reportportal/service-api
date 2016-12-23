db.project.update(
    {},
    {$set: {"configuration.statisticsCalculationStrategy": "STEP_BASED"}},
    {
        multi: true
    }
);