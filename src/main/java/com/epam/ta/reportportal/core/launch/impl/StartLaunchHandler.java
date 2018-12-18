/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.StartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
class StartLaunchHandler implements com.epam.ta.reportportal.core.launch.StartLaunchHandler {

	private final LaunchRepository launchRepository;
	private final MessageBus messageBus;

	@Autowired
	public StartLaunchHandler(LaunchRepository launchRepository, MessageBus messageBus) {
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
	}

	@Override
	@Transactional
	public StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
		validateRoles(user, projectDetails, startLaunchRQ);

		Launch launch = new LaunchBuilder().addStartRQ(startLaunchRQ).addAttributes(startLaunchRQ.getAttributes())
				.addProject(projectDetails.getProjectId())
				.addUser(user.getUserId())
				.get();
		launchRepository.save(launch);
		launchRepository.refresh(launch);

		messageBus.publishActivity(new LaunchStartedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId()));

		StartLaunchRS response = new StartLaunchRS();
		response.setId(launch.getId());
		response.setNumber(launch.getNumber());
		return response;
	}

	/**
	 * Validate {@link ReportPortalUser} credentials
	 *
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param startLaunchRQ  {@link StartLaunchRQ}
	 */
	private void validateRoles(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
		if (startLaunchRQ.getMode() == Mode.DEBUG && projectDetails.getProjectRole() == ProjectRole.CUSTOMER) {
			throw new ReportPortalException(ErrorType.ACCESS_DENIED);
		}
	}
}