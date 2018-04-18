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

import com.epam.ta.reportportal.core.externalsystem.ExternalSystemStrategy;
import com.epam.ta.reportportal.core.externalsystem.StrategyProvider;
import com.epam.ta.reportportal.core.externalsystem.handler.ICreateExternalSystemHandler;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.events.ExternalSystemCreatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.ExternalSystemBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Handler realization of {@link ICreateExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateExternalSystemHandler implements ICreateExternalSystemHandler {

	@Autowired
	private StrategyProvider strategyProvider;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private Provider<ExternalSystemBuilder> builder;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public EntryCreatedRS createExternalSystem(CreateExternalSystemRQ createRQ, String projectName, String username) {
		Project project = projectRepository.findByName(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		/* Remove trailing slash if exists */
		if (createRQ.getUrl().endsWith("/")) {
			createRQ.setUrl(createRQ.getUrl().substring(0, createRQ.getUrl().length() - 1));
		}

		String sysUrl = createRQ.getUrl();
		String sysProject = createRQ.getProject();

		ExternalSystem exist = externalSystemRepository.findByUrlAndProject(sysUrl, sysProject, projectName);
		expect(exist, isNull()).verify(EXTERNAL_SYSTEM_ALREADY_EXISTS, sysUrl + " & " + sysProject);

		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(createRQ.getExternalSystemType());
		expect(externalSystemStrategy, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, createRQ.getExternalSystemType());
		ExternalSystem details = new ExternalSystem();
		details.setUrl(createRQ.getUrl());
		details.setProject(createRQ.getProject());
		details.setAccessKey(createRQ.getAccessKey());
		details.setExternalSystemType(createRQ.getExternalSystemType());

		AuthType authType = AuthType.findByName(createRQ.getExternalSystemAuth());
		expect(authType, notNull()).verify(INCORRECT_AUTHENTICATION_TYPE, createRQ.getExternalSystemAuth());
		details.setExternalSystemAuth(authType);
		if (authType.requiresPassword()) {
			details.setUsername(createRQ.getUsername());
			details.setPassword(createRQ.getPassword());
			details.setDomain(createRQ.getDomain());

		}
		expect(externalSystemStrategy.connectionTest(details), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, projectName);

		ExternalSystem newOne = builder.get().addExternalSystem(createRQ, projectName).build();
		ExternalSystem createOne;
		try {
			createOne = externalSystemRepository.save(newOne);
			List<String> externalSystemIds = project.getConfiguration().getExternalSystem();
			externalSystemIds.add(createOne.getId());
			project.getConfiguration().setExternalSystem(externalSystemIds);
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during creating ExternalSystem", e);
		}
		eventPublisher.publishEvent(new ExternalSystemCreatedEvent(createOne, username));
		return new EntryCreatedRS(createOne.getId());
	}

}