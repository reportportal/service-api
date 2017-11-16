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

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.launch.IDeleteLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.events.LaunchDeletedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.Preconditions.IN_PROGRESS;
import static com.epam.ta.reportportal.commons.Preconditions.hasProjectRoles;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.database.entity.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.TO_INVESTIGATE;
import static com.epam.ta.reportportal.database.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Default implementation of {@link IDeleteLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteLaunchHandler implements IDeleteLaunchHandler {

	private final LaunchRepository launchRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	private TestItemRepository itemRepository;

	@Autowired
	private ILogIndexer logIndexer;

	@Autowired
	public DeleteLaunchHandler(ApplicationEventPublisher eventPublisher, LaunchRepository launchRepository,
			ProjectRepository projectRepository, UserRepository userRepository) {
		this.eventPublisher = eventPublisher;
		this.launchRepository = launchRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
	}

	@Override
	public OperationCompletionRS deleteLaunch(String launchId, String projectName, String principal) {
		Launch launch = launchRepository.findOne(launchId);
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);

		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		User user = userRepository.findOne(principal);
		validate(launch, user, project);
		try {
			launchRepository.delete(singletonList(launchId));
			logIndexer.cleanIndex(projectName, itemRepository.findIdsNotInIssueType(TO_INVESTIGATE.getLocator(), launchId));
		} catch (Exception exp) {
			throw new ReportPortalException("Error while Launch deleting.", exp);
		}
		eventPublisher.publishEvent(new LaunchDeletedEvent(launch, principal));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	@Override
	public OperationCompletionRS deleteLaunches(String[] ids, String projectName, String userName) {
		final List<String> toDelete = asList(ids);
		final List<Launch> launches = launchRepository.find(toDelete);
		final User user = userRepository.findOne(userName);
		final Project project = projectRepository.findOne(projectName);
		launches.forEach(launch -> validate(launch, user, project));
		launches.forEach(launch -> logIndexer.cleanIndex(projectName,
				itemRepository.findIdsNotInIssueType(TO_INVESTIGATE.getLocator(), launch.getId())
		));
		launchRepository.delete(toDelete);
		launches.forEach(launch -> eventPublisher.publishEvent(new LaunchDeletedEvent(launch, userName)));
		return new OperationCompletionRS("All selected launches have been successfully deleted");
	}

	private void validate(Launch launch, User user, Project project) {
		expect(launch.getProjectRef(), equalTo(project.getName())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Target launch '{}' not under specified project '{}'", launch.getId(), project.getName())
		);

		expect(launch, not(IN_PROGRESS)).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete launch '{}' in progress state", launch.getId())
		);

		if (user.getRole() != ADMINISTRATOR && !user.getId().equalsIgnoreCase(launch.getUserRef())) {
			/* Only PROJECT_MANAGER roles could delete launches */
			UserConfig userConfig = ProjectUtils.findUserConfigByLogin(project, user.getId());
			expect(userConfig, hasProjectRoles(singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
		}
	}
}