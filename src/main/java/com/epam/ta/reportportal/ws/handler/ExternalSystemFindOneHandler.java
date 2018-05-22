package com.epam.ta.reportportal.ws.handler;

import com.epam.ta.reportportal.store.database.entity.external.ExternalSystem;

public interface ExternalSystemFindOneHandler {

	ExternalSystem findById(String systemId);
}
