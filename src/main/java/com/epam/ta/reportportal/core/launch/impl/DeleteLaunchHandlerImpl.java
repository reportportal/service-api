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

import com.epam.reportportal.events.ElementsDeletedEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.ElementsCounterService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchDeletedEvent;
import com.epam.ta.reportportal.core.launch.DeleteLaunchHandler;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;
import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
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

	private final ContentRemover<Launch> launchContentRemover;

	private final LaunchRepository launchRepository;

	private final MessageBus messageBus;

	private final LogIndexer logIndexer;

	private final AttachmentRepository attachmentRepository;

	private final ApplicationEventPublisher eventPublisher;

	private final ElementsCounterService elementsCounterService;

	@Autowired
	public DeleteLaunchHandlerImpl(ContentRemover<Launch> launchContentRemover, LaunchRepository launchRepository, MessageBus messageBus,
			LogIndexer logIndexer, AttachmentRepository attachmentRepository, ApplicationEventPublisher eventPublisher,
			ElementsCounterService elementsCounterService) {
		this.launchContentRemover = launchContentRemover;
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
		this.logIndexer = logIndexer;
		this.attachmentRepository = attachmentRepository;
		this.eventPublisher = eventPublisher;
		this.elementsCounterService = elementsCounterService;
	}

	public OperationCompletionRS deleteLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		validate(launch, user, projectDetails);
		final Long numberOfLaunchElements = elementsCounterService.countNumberOfLaunchElements(launchId);

		logIndexer.indexLaunchesRemove(projectDetails.getProjectId(), Lists.newArrayList(launchId));
		launchContentRemover.remove(launch);
		launchRepository.delete(launch);
		attachmentRepository.moveForDeletionByLaunchId(launchId);

		messageBus.publishActivity(new LaunchDeletedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId(), user.getUsername()));
		eventPublisher.publishEvent(new ElementsDeletedEvent(launchId, launch.getProjectId(), numberOfLaunchElements));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	public DeleteBulkRS deleteLaunches(DeleteBulkRQ deleteBulkRQ, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		List<Long> notFound = Lists.newArrayList();
		List<ReportPortalException> exceptions = Lists.newArrayList();
		Map<Launch, Long> toDelete = Maps.newHashMap();
		List<Long> launchIds = Lists.newArrayList();

		deleteBulkRQ.getIds().forEach(id -> {
			Optional<Launch> optionalLaunch = launchRepository.findById(id);
			if (optionalLaunch.isPresent()) {
				Launch launch = optionalLaunch.get();
				try {
					validate(launch, user, projectDetails);
					Long numberOfLaunchElements = elementsCounterService.countNumberOfLaunchElements(launch.getId());
					toDelete.put(launch, numberOfLaunchElements);
					launchIds.add(id);
				} catch (ReportPortalException ex) {
					exceptions.add(ex);
				}
			} else {
				notFound.add(id);
			}
		});

		if (CollectionUtils.isNotEmpty(launchIds)) {
			logIndexer.indexLaunchesRemove(projectDetails.getProjectId(), launchIds);
			toDelete.keySet().forEach(launchContentRemover::remove);
			launchRepository.deleteAll(toDelete.keySet());
			attachmentRepository.moveForDeletionByLaunchIds(launchIds);
		}

		toDelete.entrySet().forEach(entry -> {
			LaunchActivityResource launchActivity = TO_ACTIVITY_RESOURCE.apply(entry.getKey());
			messageBus.publishActivity(new LaunchDeletedEvent(launchActivity, user.getUserId(), user.getUsername()));
			eventPublisher.publishEvent(new ElementsDeletedEvent(entry.getKey().getId(), entry.getKey().getProjectId(), entry.getValue()));
		});

		return new DeleteBulkRS(launchIds, notFound, exceptions.stream().map(ex -> {
			ErrorRS errorResponse = new ErrorRS();
			errorResponse.setErrorType(ex.getErrorType());
			errorResponse.setMessage(ex.getMessage());
			return errorResponse;
		}).collect(Collectors.toList()));
	}

	/**
	 * Validate user credentials and {@link Launch#getStatus()}
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
		}
	}
}