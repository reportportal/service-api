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

import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.ProjectRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link IStartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
class StartLaunchHandler implements IStartLaunchHandler {
	private final LaunchRepository launchRepository;
	private final ProjectRepository projectRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public StartLaunchHandler(LaunchRepository launchRepository, ProjectRepository projectRepository,
			ApplicationEventPublisher eventPublisher) {
		this.launchRepository = launchRepository;
		this.projectRepository = projectRepository;
		this.eventPublisher = eventPublisher;
	}

	/*
		 * (non-Javadoc)
		 *k
		 * @see
		 * com.epam.ta.reportportal.ws.IStartLaunchHandler#startLaunch(java.lang
		 * .String, com.epam.ta.reportportal.ws.model.StartLaunchRQ)
		 */
	public StartLaunchRS startLaunch(String username, String projectName, StartLaunchRQ startLaunchRQ) {
		//TODO replace with new uat
		//		ProjectUser projectUser = projectRepository.selectProjectUser(projectName, username);
		//		if (startLaunchRQ.getMode() == Mode.DEBUG) {
		//			if (projectUser.getProjectRole() == ProjectRoleEnum.CUSTOMER) {
		//				startLaunchRQ.setMode(Mode.DEFAULT);
		//			}
		//		}
		Launch launch = new LaunchBuilder().addStartRQ(startLaunchRQ).addProject(1).addUser(1L).addTags(startLaunchRQ.getTags()).get();
		launch.setStatus(StatusEnum.IN_PROGRESS);

		//launch.setApproximateDuration(calculateApproximateDuration(projectName, startLaunchRQ.getName(), 5));

		launchRepository.save(launch);
		launchRepository.refresh(launch);
		//eventPublisher.publishEvent(new LaunchStartedEvent(launch));
		return new StartLaunchRS(launch.getId().toString(), launch.getNumber().longValue());
	}

	private double calculateApproximateDuration(String projectName, String launchName, int limit) {
		//		Set<FilterCondition> conditions = ImmutableSet.<FilterCondition>builder().add(new FilterCondition(EQUALS, false, launchName, NAME))
		//				.add(new FilterCondition(EQUALS, false, projectName, PROJECT))
		//				.add(new FilterCondition(IN, true, STOPPED.name() + "," + INTERRUPTED.name() + "," + IN_PROGRESS.name(), STATUS))
		//				.add(new FilterCondition(EQUALS, false, DEFAULT.name(), MODE_CRITERIA))
		//				.build();
		//		Filter filter = new Filter(Launch.class, conditions);
		//		Sort sort = new Sort(new Sort.Order(DESC, "start_time"));
		//		List<Launch> launches = launchRepository.findByFilterWithSortingAndLimit(filter, sort, limit);
		//		return launches.stream().mapToLong(it -> it.getEndTime().getTime() - it.getStartTime().getTime()).average().orElse(0);
		return 0;
	}
}