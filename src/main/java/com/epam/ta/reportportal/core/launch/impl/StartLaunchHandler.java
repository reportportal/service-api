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
import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.store.commons.EntityUtils.takeProjectDetails;

/**
 * Default implementation of {@link IStartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
class StartLaunchHandler implements IStartLaunchHandler {

	private LaunchRepository launchRepository;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public StartLaunchRS startLaunch(ReportPortalUser user, String projectName, StartLaunchRQ startLaunchRQ) {
		validateRoles(user, projectName, startLaunchRQ);

		Launch launch = new LaunchBuilder().addStartRQ(startLaunchRQ)
				.addProject(user.getProjectDetails().get(projectName).getProjectId())
				.addUser(user.getUserId())
				.addTags(startLaunchRQ.getTags())
				.get();
		launchRepository.saveAndFlush(launch);
		launchRepository.refresh(launch);
		//eventPublisher.publishEvent(new LaunchStartedEvent(launch));
		return new StartLaunchRS(launch.getId(), launch.getNumber());
	}

	private void validateRoles(ReportPortalUser user, String projectName, StartLaunchRQ startLaunchRQ) {
		ReportPortalUser.ProjectDetails projectDetails = takeProjectDetails(user, projectName);
		if (startLaunchRQ.getMode() == Mode.DEBUG) {
			if (projectDetails.getProjectRole() == ProjectRole.CUSTOMER) {
				startLaunchRQ.setMode(Mode.DEFAULT);
			}
		}
	}
}