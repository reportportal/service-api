package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.store.database.entity.external.ExternalSystem;
import com.epam.ta.reportportal.ws.handler.ExternalSystemFindOneHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalSystemFindOneHandlerImpl implements ExternalSystemFindOneHandler {

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Override
	public ExternalSystem findById(String systemId) {

		return externalSystemRepository.findById(Long.valueOf(systemId)).orElse(null);
	}
}
