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
import com.epam.ta.reportportal.core.events.activity.LaunchDeletedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.DeleteLaunchesRS;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Arrays.stream;

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

	//TODO Analyzer
	public OperationCompletionRS deleteLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		validate(launch, user, projectDetails);
		launchRepository.delete(launch);

		//		logIndexer.cleanIndex(
		//				projectName, itemRepository.selectIdsNotInIssueByLaunch(launchId, TestItemIssueType.TO_INVESTIGATE.getLocator()));
		messageBus.publishActivity(new LaunchDeletedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId()));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	//TODO Analyzer
	public DeleteLaunchesRS deleteLaunches(Long[] ids, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<Long> notFound = Lists.newArrayList();
		List<ReportPortalException> exceptions = Lists.newArrayList();
		List<Long> noPermissions = Lists.newArrayList();
		List<Launch> toDelete = Lists.newArrayList();

		stream(ids).forEach(id -> {
			Optional<Launch> optionalLaunch = launchRepository.findById(id);
			if (optionalLaunch.isPresent()) {
				Launch launch = optionalLaunch.get();
				Optional<ReportPortalException> permissionsExceptionOptional = validatePermissions(launch, user, projectDetails);
				Optional<ReportPortalException> projectExceptionOptional = validateProject(launch, projectDetails);
				Optional<ReportPortalException> statusExceptionOptional = validateStatus(launch);
				if (!permissionsExceptionOptional.isPresent() && !projectExceptionOptional.isPresent()
						&& !statusExceptionOptional.isPresent()) {
					toDelete.add(launch);
				}
				permissionsExceptionOptional.ifPresent(ex -> {
					exceptions.add(ex);
					noPermissions.add(id);
				});
				projectExceptionOptional.ifPresent(exceptions::add);
				statusExceptionOptional.ifPresent(exceptions::add);
			} else {
				notFound.add(id);
			}
		});

		//		launches.forEach(launch -> logIndexer.cleanIndex(projectName,
		//				itemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueType.TO_INVESTIGATE.getLocator())
		//		));
		launchRepository.deleteAll(toDelete);
		toDelete.stream().map(TO_ACTIVITY_RESOURCE).forEach(r -> new LaunchDeletedEvent(r, user.getUserId()));
		return new DeleteLaunchesRS(toDelete.stream().map(Launch::getId).collect(Collectors.toList()),
				notFound,
				noPermissions,
				exceptions.stream().map(ex -> {
					ErrorRS errorResponse = new ErrorRS();
					errorResponse.setErrorType(ex.getErrorType());
					errorResponse.setMessage(ex.getMessage());
					return errorResponse;
				}).collect(Collectors.toList())
		);
	}

	/**
	 * Validate user credentials and {@link Launch#status}
	 *
	 * @param launch         {@link Launch}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 */
	private void validate(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION, formattedSupplier(
				"Target launch '{}' not under specified project '{}'",
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

	private Optional<ReportPortalException> validateProject(Launch launch, ReportPortalUser.ProjectDetails projectDetails) {
		return launch.getProjectId().equals(projectDetails.getProjectId()) ?
				Optional.empty() :
				Optional.of(new ReportPortalException(FORBIDDEN_OPERATION, formattedSupplier(
						"Target launch '{}' not under specified project '{}'",
								launch.getId(),
								projectDetails.getProjectId()
						)
				));
	}

	private Optional<ReportPortalException> validateStatus(Launch launch) {
		return !launch.getStatus().equals(StatusEnum.IN_PROGRESS) ?
				Optional.empty() :
				Optional.of(new ReportPortalException(LAUNCH_IS_NOT_FINISHED,
						formattedSupplier("Unable to delete launch '{}' in progress state", launch.getId())
				));
	}

	private Optional<ReportPortalException> validatePermissions(Launch launch, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUsername(), launch.getUser().getLogin())) {
			return projectDetails.getProjectRole().equals(PROJECT_MANAGER) ?
					Optional.empty() :
					Optional.of(new ReportPortalException(ACCESS_DENIED));
		}
		return Optional.empty();
	}
}