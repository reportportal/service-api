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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.ICreateExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuthFactory;
import com.epam.ta.reportportal.util.ProjectUtils;
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
 * @author Pavel Bortnik
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
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);

		//ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(createRQ.getExternalSystemType());
		//expect(externalSystemStrategy, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, createRQ.getExternalSystemType());

		BugTrackingSystem bugTrackingSystem = new BugTrackingSystemBuilder().addUrl(createRQ.getUrl())
				.addBugTrackingSystemType(createRQ.getExternalSystemType())
				.addBugTrackingProject(createRQ.getProject())
				.addProject(projectDetails.getProjectId())
				.get();

		//checkUnique(bugTrackingSystem, projectDetails.getProjectId());

		//expect(externalSystemStrategy.connectionTest(externalSystem), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, projectName);

		bugTrackingSystemRepository.save(bugTrackingSystem);
		//eventPublisher.publishEvent(new IntegrationCreatedEvent(createOne, username));
		return new EntryCreatedRS(bugTrackingSystem.getId());
	}

	//TODO probably could be handled by database
	private void checkUnique(BugTrackingSystem bugTrackingSystem, Long projectId) {
		bugTrackingSystemRepository.findByUrlAndBtsProjectAndProjectId(
				bugTrackingSystem.getUrl(), bugTrackingSystem.getBtsProject(), projectId)
				.ifPresent(it -> new ReportPortalException(ErrorType.EXTERNAL_SYSTEM_ALREADY_EXISTS,
						bugTrackingSystem.getUrl() + " & " + bugTrackingSystem.getBtsProject()
				));
	}

}