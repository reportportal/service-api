package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.store.database.entity.integration.Integration;

public interface IntegrationFindOneHandler {

	Integration findById(String id);
}
