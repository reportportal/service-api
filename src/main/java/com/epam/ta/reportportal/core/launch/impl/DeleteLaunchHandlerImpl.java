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
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchDeletedEvent;
import com.epam.ta.reportportal.core.launch.DeleteLaunchHandler;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.DeleteLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class DeleteLaunchHandlerImpl implements DeleteLaunchHandler {

	private final LaunchRepository launchRepository;

	private final LogRepository logRepository;

	private final MessageBus messageBus;

	private final LogIndexer logIndexer;

	private final AttachmentRepository attachmentRepository;

	@Autowired
	public DeleteLaunchHandlerImpl(LaunchRepository launchRepository, LogRepository logRepository, MessageBus messageBus,
			LogIndexer logIndexer, AttachmentRepository attachmentRepository) {
		this.launchRepository = launchRepository;
		this.logRepository = logRepository;
		this.messageBus = messageBus;
		this.logIndexer = logIndexer;
		this.attachmentRepository = attachmentRepository;
	}

	public OperationCompletionRS deleteLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		validate(launch, user, projectDetails);
		logIndexer.cleanIndex(projectDetails.getProjectId(),
				logRepository.findItemLogIdsByLaunchIdAndLogLevelGte(launchId, LogLevel.ERROR.toInt())
		);

		launchRepository.delete(launch);

		attachmentRepository.moveForDeletionByLaunchId(launchId);

		messageBus.publishActivity(new LaunchDeletedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(), user.getUsername()));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	public DeleteBulkRS deleteLaunches(DeleteBulkRQ deleteBulkRQ, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<Long> notFound = Lists.newArrayList();
		List<ReportPortalException> exceptions = Lists.newArrayList();
		List<Launch> toDelete = Lists.newArrayList();

		deleteBulkRQ.getIds().forEach(id -> {
			Optional<Launch> optionalLaunch = launchRepository.findById(id);
			if (optionalLaunch.isPresent()) {
				Launch launch = optionalLaunch.get();
				try {
					validate(launch, user, projectDetails);
					toDelete.add(launch);
				} catch (ReportPortalException ex) {
					exceptions.add(ex);
				}
			} else {
				notFound.add(id);
			}
		});

		List<Long> launchIds = toDelete.stream().map(Launch::getId).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(launchIds)) {
			logIndexer.cleanIndex(projectDetails.getProjectId(),
					logRepository.findItemLogIdsByLaunchIdsAndLogLevelGte(launchIds, LogLevel.ERROR.toInt())
			);
			launchRepository.deleteAll(toDelete);
			attachmentRepository.moveForDeletionByLaunchIds(launchIds);
		}

		toDelete.stream()
				.map(TO_ACTIVITY_RESOURCE)
				.forEach(it -> messageBus.publishActivity(new LaunchDeletedEvent(it, user.getUserId(), user.getUsername())));

		return new DeleteBulkRS(launchIds, notFound, exceptions.stream().map(ex -> {
			ErrorRS errorResponse = new ErrorRS();
			errorResponse.setErrorType(ex.getErrorType());
			errorResponse.setMessage(ex.getMessage());
			return errorResponse;
		}).collect(Collectors.toList()));
	}

	/**
	 * Validate user credentials and {@link Launch#status}
	 *
	 * @param launch         {@link Launch}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link ReportPortalUser.ProjectDetails}
	 */
	private void validate(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		expect(launch, not(l -> StatusEnum.IN_PROGRESS.equals(l.getStatus()))).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete launch '{}' in progress state", launch.getId())
		);
		if (!UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
					formattedSupplier("Target launch '{}' not under specified project '{}'", launch.getId(), projectDetails.getProjectId())
			);
			/* Only PROJECT_MANAGER roles could delete launches */
			if (projectDetails.getProjectRole().lowerThan(PROJECT_MANAGER)) {
				expect(user.getUserId(), Predicate.isEqual(launch.getUserId())).verify(ACCESS_DENIED, "You are not launch owner.");
			}
		}
	}
}