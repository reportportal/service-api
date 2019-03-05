/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.StartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
class StartLaunchHandlerImpl implements com.epam.ta.reportportal.core.launch.StartLaunchHandler {

	private final LaunchRepository launchRepository;
	private final MessageBus messageBus;

	@Autowired
	public StartLaunchHandlerImpl(LaunchRepository launchRepository, MessageBus messageBus) {
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
	}

	@Override
	@Transactional
	public StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
		validateRoles(projectDetails, startLaunchRQ);

		Launch launch = new LaunchBuilder().addStartRQ(startLaunchRQ)
				.addAttributes(startLaunchRQ.getAttributes())
				.addProject(projectDetails.getProjectId())
				.addUser(user.getUserId())
				.get();
		launch = launchRepository.save(launch);
		launchRepository.refresh(launch);

		messageBus.publishActivity(new LaunchStartedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId()));

		StartLaunchRS response = new StartLaunchRS();
		response.setId(launch.getId());
		response.setNumber(launch.getNumber());
		return response;
	}

	/**
	 * Validate {@link ReportPortalUser} credentials. User with a {@link ProjectRole#CUSTOMER} role can't report
	 * launches in a debug mode.
	 *
	 * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
	 * @param startLaunchRQ  {@link StartLaunchRQ}
	 */
	private void validateRoles(ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
		expect(
				startLaunchRQ.getMode() == Mode.DEBUG && projectDetails.getProjectRole() == ProjectRole.CUSTOMER,
				Predicate.isEqual(false)
		).verify(ErrorType.FORBIDDEN_OPERATION);
	}
}