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

import static com.epam.ta.reportportal.database.entity.Launch.*;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.search.Condition.EQUALS;
import static com.epam.ta.reportportal.database.search.Condition.IN;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epam.ta.reportportal.events.LaunchStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchMetaInfoRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

/**
 * Default implementation of {@link IStartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
class StartLaunchHandler implements IStartLaunchHandler {

	private final LaunchRepository launchRepository;
	private final LazyReference<LaunchBuilder> launchBuilder;
	private final LaunchMetaInfoRepository launchCounter;
	private final ProjectRepository projectRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public StartLaunchHandler(LaunchMetaInfoRepository launchCounter, ProjectRepository projectRepository,
			LaunchRepository launchRepository, ApplicationEventPublisher eventPublisher,
			@Qualifier("launchBuilder.reference") LazyReference<LaunchBuilder> launchBuilder) {
		this.launchCounter = launchCounter;
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.eventPublisher = eventPublisher;
		this.launchBuilder = launchBuilder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.epam.ta.reportportal.ws.IStartLaunchHandler#startLaunch(java.lang
	 * .String, com.epam.ta.reportportal.ws.model.StartLaunchRQ)
	 */
	@Override
	public EntryCreatedRS startLaunch(String username, String projectName, StartLaunchRQ startLaunchRQ) {

		if (startLaunchRQ.getMode() == DEBUG) {

			Project project = projectRepository.findByName(projectName);
			Project.UserConfig userConfig = project.getUsers().get(username);
			if (userConfig.getProjectRole() == ProjectRole.CUSTOMER) {
				startLaunchRQ.setMode(DEFAULT);
			}
		}

		// userName and projectName validations here is redundant, user name and
		// projectName have already validated by spring security in controller
		Launch launch = launchBuilder.get().addStartRQ(startLaunchRQ).addProject(projectName).addStatus(IN_PROGRESS).addUser(username)
				.build();
		/*
		 * Retrieve and set number of launch with provided name
		 */
		launch.setNumber(launchCounter.getLaunchNumber(launch.getName(), projectName));
		launch.setApproximateDuration(calculateApproximateDuration(projectName, startLaunchRQ.getName(), 5));

		launchRepository.save(launch);

		eventPublisher.publishEvent(new LaunchStartedEvent(launch));
		return new EntryCreatedRS(launch.getId());

	}

	private double calculateApproximateDuration(String projectName, String launchName, int limit) {
		Set<FilterCondition> conditions = new HashSet<>();
		conditions.add(new FilterCondition(EQUALS, false, launchName, NAME));
		conditions.add(new FilterCondition(EQUALS, false, projectName, PROJECT));
		conditions.add(new FilterCondition(IN, true, STOPPED.name() + "," + INTERRUPTED.name() + "," + IN_PROGRESS.name(), STATUS));
		conditions.add(new FilterCondition(EQUALS, false, DEFAULT.name(), MODE_CRITERIA));
		Filter filter = new Filter(Launch.class, conditions);
		Sort sort = new Sort(new Sort.Order(DESC, "start_time"));
		List<Launch> launches = launchRepository.findByFilterWithSortingAndLimit(filter, sort, limit);
		return launches.stream().mapToLong(it -> it.getEndTime().getTime() - it.getStartTime().getTime()).average().orElse(0);
	}
}