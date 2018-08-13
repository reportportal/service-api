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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	public StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
		validateRoles(user, projectDetails, startLaunchRQ);

		Launch launch = new LaunchBuilder().addStartRQ(startLaunchRQ)
				.addProject(projectDetails.getProjectId())
				.addUser(user.getUserId())
				.addTags(startLaunchRQ.getTags())
				.get();
		launchRepository.saveAndFlush(launch);
		launchRepository.refresh(launch);

		messageBus.publishActivity(new LaunchStartedEvent(launch));

		return new StartLaunchRS(launch.getId(), launch.getNumber());
	}

	/**
	 * TODO document this
	 *
	 * @param user
	 * @param projectDetails
	 * @param startLaunchRQ
	 */
	private void validateRoles(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, StartLaunchRQ startLaunchRQ) {
		if (startLaunchRQ.getMode() == Mode.DEBUG) {
			if (projectDetails.getProjectRole() == ProjectRole.CUSTOMER) {
				startLaunchRQ.setMode(Mode.DEFAULT);
			}
		}
	}
}