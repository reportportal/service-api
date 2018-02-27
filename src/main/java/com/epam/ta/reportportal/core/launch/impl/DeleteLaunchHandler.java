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

import com.epam.ta.reportportal.core.launch.IDeleteLaunchHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.user.Users;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.store.commons.Predicates.not;
import static com.epam.ta.reportportal.store.database.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_IS_NOT_FINISHED;
import static java.util.Arrays.asList;

/**
 * Default implementation of {@link IDeleteLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteLaunchHandler implements IDeleteLaunchHandler {

	private ApplicationEventPublisher eventPublisher;

	private LaunchRepository launchRepository;

	private TestItemRepository itemRepository;

	//	private ILogIndexer logIndexer;

	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setItemRepository(TestItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	//	@Autowired
	//	public void setLogIndexer(ILogIndexer logIndexer) {
	//		this.logIndexer = logIndexer;
	//	}

	//TODO replace project and user validations with new uat. Activities
	public OperationCompletionRS deleteLaunch(Long launchId, String projectName, String principal) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

		//		Project project = projectRepository.findById(projectName)
		//				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectName));
		//		User user = userRepository.findById(principal).get();
		//		validate(launch, user, project);

		launchRepository.delete(launch);
		//		logIndexer.cleanIndex(
		//				projectName, itemRepository.selectIdsNotInIssueByLaunch(launchId, TestItemIssueType.TO_INVESTIGATE.getLocator()));

		//		eventPublisher.publishEvent(new LaunchDeletedEvent(launch, principal));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	//TODO replace project and user validations with new uat. Analyzer indexing. Activities
	public OperationCompletionRS deleteLaunches(Long[] ids, String projectName, String userName) {
		List<Launch> launches = launchRepository.findAllById(asList(ids));

		//		final User user = userRepository.findById(userName).get();
		//		final Project project = projectRepository.findById(projectName).get();
		// 		launches.forEach(launch -> validate(launch, user, project));

		//		launches.forEach(launch -> logIndexer.cleanIndex(projectName,
		//				itemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueType.TO_INVESTIGATE.getLocator())
		//		));
		launchRepository.deleteAll(launches);

		//		launches.forEach(launch -> eventPublisher.publishEvent(new LaunchDeletedEvent(launch, userName)));
		return new OperationCompletionRS("All selected launches have been successfully deleted");
	}

	private void validate(Launch launch, Users user, Project project) {
		expect(launch.getProjectId(), equalTo(project.getName())).verify(
				FORBIDDEN_OPERATION,
				formattedSupplier("Target launch '{}' not under specified project '{}'", launch.getId(), project.getName())
		);

		expect(launch, not(l -> l.getStatus().equals(IN_PROGRESS))).verify(
				LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete launch '{}' in progress state", launch.getId())
		);

		//TODO replace with new uat
		//		if (user.getRole() != ADMINISTRATOR && !user.getId().equalsIgnoreCase(launch.getUserRef())) {
		//			/* Only PROJECT_MANAGER roles could delete launches */
		//			UserConfig userConfig = ProjectUtils.findUserConfigByLogin(project, user.getId());
		//			expect(userConfig, hasProjectRoles(singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
		//		}
	}
}