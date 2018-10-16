/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.ICreateExternalSystemHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystemAuthFactory;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.builders.BugTrackingSystemBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.CreateExternalSystemRQ;
import org.springframework.beans.factory.annotation.Autowired;
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
	private MessageBus messageBus;

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
		//messageBus.publishActivity(new IntegrationCreatedEvent(integration, user.getUserId()));
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