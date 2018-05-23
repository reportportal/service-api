package com.epam.ta.reportportal.ws.handler.impl;

import com.epam.ta.reportportal.store.database.dao.IntegrationRepository;
import com.epam.ta.reportportal.store.database.entity.integration.Integration;
import com.epam.ta.reportportal.ws.handler.IntegrationFindOneHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntegrationFindOneHandlerImpl implements IntegrationFindOneHandler {

	@Autowired
	private IntegrationRepository integrationRepository;

	@Override
	public Integration findById(String id) {

		return integrationRepository.findById(Long.valueOf(id)).orElse(null);
	}
}
