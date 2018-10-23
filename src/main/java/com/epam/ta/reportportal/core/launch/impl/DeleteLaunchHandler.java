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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchDeletedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Arrays.asList;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.DeleteLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class DeleteLaunchHandler implements com.epam.ta.reportportal.core.launch.DeleteLaunchHandler {

	private MessageBus messageBus;

	private LaunchRepository launchRepository;

	//	private ILogIndexer logIndexer;

	@Autowired
	public void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	//	@Autowired
	//	public void setLogIndexer(ILogIndexer logIndexer) {
	//		this.logIndexer = logIndexer;
	//	}

	//TODO Analyzer, Activities
	public OperationCompletionRS deleteLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		validate(launch, user, projectDetails);
		launchRepository.delete(launch);

		//		logIndexer.cleanIndex(
		//				projectName, itemRepository.selectIdsNotInIssueByLaunch(launchId, TestItemIssueType.TO_INVESTIGATE.getLocator()));
		messageBus.publishActivity(new LaunchDeletedEvent(launch, user.getUserId()));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	//TODO Analyzer, Activities
	public OperationCompletionRS deleteLaunches(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<Launch> launches = launchRepository.findAllById(asList(ids));
		launches.forEach(l -> validate(l, user, projectDetails));
		//		launches.forEach(launch -> logIndexer.cleanIndex(projectName,
		//				itemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueType.TO_INVESTIGATE.getLocator())
		//		));
		launchRepository.deleteAll(launches);

		launches.forEach(l -> messageBus.publishActivity(new LaunchDeletedEvent(l, user.getUserId())));
		return new OperationCompletionRS("All selected launches have been successfully deleted");
	}

	/**
	 * Validate user credentials and {@link Launch#status}
	 *
	 * @param launch         {@link Launch}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 */
	private void validate(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("FilterTarget launch '{}' not under specified project '{}'",
						launch.getId(),
						projectDetails.getProjectId()
				)
		);
		expect(launch, not(l -> l.getStatus().equals(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete launch '{}' in progress state", launch.getId())
		);
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUsername(), launch.getUser().getLogin())) {
			/* Only PROJECT_MANAGER roles could delete launches */
			expect(projectDetails.getProjectRole(), equalTo(PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}
	}
}