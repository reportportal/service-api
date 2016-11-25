/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package com.epam.ta.reportportal.core.externalsystem.handler.impl;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

import java.util.List;

import com.epam.ta.reportportal.core.externalsystem.ExternalSystemStrategy;
import com.epam.ta.reportportal.core.externalsystem.StrategyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.externalsystem.handler.IGetTicketHandler;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

/**
 * Default implementation of {@link IGetTicketHandler}
 * 
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetTicketHandler implements IGetTicketHandler {

	@Autowired
	private StrategyProvider strategyProvider;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Override
	public Ticket getTicket(String ticketId, String projectName, String systemId) {
		Project project = projectRepository.findOne(projectName);
		// Project validated on controller level
		List<String> ids = project.getConfiguration().getExternalSystem();
		expect(ids, notNull()).verify(PROJECT_NOT_CONFIGURED, projectName);
		// TODO update if project will be used different systems
		ExternalSystem system = externalSystemRepository.findOne(systemId);
		expect(system, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, systemId);
		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType().name(), project);
		return externalSystemStrategy.getTicket(ticketId, system).orElse(null);
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, String projectName, String systemId) {
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		ExternalSystem system = externalSystemRepository.findOne(systemId);
		expect(system, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, systemId);
		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType().name(), project);
		return externalSystemStrategy.getTicketFields(ticketType, system);
	}
}