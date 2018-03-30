/*
 * Copyright 2017 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.externalsystem.handler.ICreateExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuth;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuthFactory;
import com.epam.ta.reportportal.store.database.entity.enums.AuthType;
import com.epam.ta.reportportal.ws.converter.builders.BugTrackingSystemBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Handler realization of {@link ICreateExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateExternalSystemHandler implements ICreateExternalSystemHandler {
/*
	@Autowired
	private StrategyProvider strategyProvider;*/

	@Autowired
	private BugTrackingSystemRepository bugTrackingSystemRepository;

	@Autowired
	private BugTrackingSystemAuthFactory bugTrackingSystemAuthFactory;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public EntryCreatedRS createExternalSystem(CreateExternalSystemRQ createRQ, String projectName, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);

		/* Remove trailing slash if exists */
		if (createRQ.getUrl().endsWith("/")) {
			createRQ.setUrl(createRQ.getUrl().substring(0, createRQ.getUrl().length() - 1));
		}

		bugTrackingSystemRepository.findByUrlAndBtsProjectAndProjectId(
				createRQ.getUrl(), createRQ.getProject(), projectDetails.getProjectId())
				.ifPresent(it -> new ReportPortalException(ErrorType.EXTERNAL_SYSTEM_ALREADY_EXISTS,
						createRQ.getUrl() + " & " + createRQ.getProject()
				));

		//ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(createRQ.getExternalSystemType());
		//expect(externalSystemStrategy, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, createRQ.getExternalSystemType());

		AuthType authType = AuthType.findByName(createRQ.getExternalSystemAuth());
		BugTrackingSystemAuth auth = bugTrackingSystemAuthFactory.createAuthObject(authType, createRQ);

		BugTrackingSystem bugTrackingSystem = new BugTrackingSystemBuilder().addExternalSystem(createRQ)
				.addSystemAuth(auth)
				.addProjectId(projectDetails.getProjectId())
				.get();

		//expect(externalSystemStrategy.connectionTest(externalSystem), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, projectName);

		bugTrackingSystemRepository.save(bugTrackingSystem);
		//eventPublisher.publishEvent(new ExternalSystemCreatedEvent(createOne, username));
		return new EntryCreatedRS(bugTrackingSystem.getId().longValue());
	}

}