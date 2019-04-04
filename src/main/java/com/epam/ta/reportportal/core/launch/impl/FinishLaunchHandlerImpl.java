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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishForcedEvent;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.item.descendant.FinishDescendantsHandler;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.util.LaunchLinkGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.jooq.enums.JStatusEnum;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;
import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.launch.util.LaunchLinkGenerator.generateLaunchLink;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link FinishLaunchHandler}
 *
 * @author Pave Bortnik
 */
@Service
@Transactional
public class FinishLaunchHandlerImpl implements FinishLaunchHandler {

	private static final String LAUNCH_STOP_DESCRIPTION = " stopped";

	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final FinishDescendantsHandler<Launch> finishDescendantsHandler;
	private final MessageBus messageBus;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public FinishLaunchHandlerImpl(LaunchRepository launchRepository, TestItemRepository testItemRepository,
			@Qualifier("finishLaunchDescendantsHandler") FinishDescendantsHandler<Launch> finishDescendantsHandler, MessageBus messageBus,
			ApplicationEventPublisher eventPublisher) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.finishDescendantsHandler = finishDescendantsHandler;
		this.messageBus = messageBus;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public Launch finishLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));

		validateRoles(launch, user, projectDetails);
		validate(launch, finishLaunchRQ);

		Optional<StatusEnum> status = StatusEnum.fromValue(finishLaunchRQ.getStatus());

		if (launchRepository.hasItemsInStatuses(launch.getId(), Lists.newArrayList(JStatusEnum.IN_PROGRESS))) {
			finishDescendantsHandler.finishDescendants(launch,
					status.orElse(StatusEnum.INTERRUPTED),
					finishLaunchRQ.getEndTime(),
					projectDetails
			);
			launch.setStatus(launchRepository.hasItemsWithStatusNotEqual(launchId, StatusEnum.PASSED) ? FAILED : PASSED);
		} else {
			launch.setStatus(status.orElseGet(() -> launchRepository.hasItemsWithStatusNotEqual(launchId, StatusEnum.PASSED) ?
					FAILED :
					PASSED));
		}

		final String desc = launch.getDescription() != null ?
				finishLaunchRQ.getDescription() != null ?
						launch.getDescription().concat(" " + finishLaunchRQ.getDescription()) :
						launch.getDescription() :
				null;

		launch = new LaunchBuilder(launch).addDescription(desc)
				.addAttributes(finishLaunchRQ.getAttributes())
				.addEndTime(finishLaunchRQ.getEndTime())
				.get();

		LaunchFinishedEvent event = new LaunchFinishedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId());
		messageBus.publishActivity(event);
		eventPublisher.publishEvent(event);

		return launch;
	}

	@Override
	public FinishLaunchRS finishLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, LaunchLinkGenerator.LinkParams linkParams) {
		Launch launch = finishLaunch(launchId, finishLaunchRQ, projectDetails, user);
		FinishLaunchRS response = new FinishLaunchRS();
		response.setId(launch.getId());
		response.setNumber(launch.getNumber());
		response.setLink(generateLaunchLink(linkParams, String.valueOf(launch.getId())));
		return response;
	}

	@Override
	public OperationCompletionRS stopLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

		validateRoles(launch, user, projectDetails);
		validate(launch, finishLaunchRQ);

		launch = new LaunchBuilder(launch).addDescription(ofNullable(finishLaunchRQ.getDescription()).orElse(ofNullable(launch.getDescription())
				.orElse("")).concat(LAUNCH_STOP_DESCRIPTION))
				.addStatus(ofNullable(finishLaunchRQ.getStatus()).orElse(STOPPED.name()))
				.addEndTime(ofNullable(finishLaunchRQ.getEndTime()).orElse(new Date()))
				.addAttributes(finishLaunchRQ.getAttributes())
				.addAttribute(new ItemAttributeResource("status", "stopped"))
				.get();

		launchRepository.save(launch);
		testItemRepository.interruptInProgressItems(launchId);

		messageBus.publishActivity(new LaunchFinishForcedEvent(TO_ACTIVITY_RESOURCE.apply(launch), user.getUserId()));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully stopped.");
	}

	@Override
	public List<OperationCompletionRS> stopLaunch(BulkRQ<FinishExecutionRQ> bulkRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		return bulkRQ.getEntities()
				.entrySet()
				.stream()
				.map(entry -> stopLaunch(entry.getKey(), entry.getValue(), projectDetails, user))
				.collect(toList());
	}

	/**
	 * Validate {@link Launch#status} and value of the {@link Launch#endTime}
	 *
	 * @param launch            {@link Launch}
	 * @param finishExecutionRQ {@link FinishExecutionRQ}
	 */
	private void validate(Launch launch, FinishExecutionRQ finishExecutionRQ) {
		expect(launch.getStatus(), equalTo(IN_PROGRESS)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		expect(finishExecutionRQ.getEndTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(FINISH_TIME_EARLIER_THAN_START_TIME,
				finishExecutionRQ.getEndTime(),
				TO_DATE.apply(launch.getStartTime()),
				launch.getId()
		);
	}

	/**
	 * Validate {@link ReportPortalUser} credentials and {@link Launch} affiliation to the {@link Project}
	 *
	 * @param launch         {@link Launch}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link ReportPortalUser.ProjectDetails}
	 */
	private void validateRoles(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			if (projectDetails.getProjectRole().lowerThan(PROJECT_MANAGER)) {
				expect(user.getUsername(), Predicate.isEqual(launch.getUser().getLogin())).verify(ACCESS_DENIED,
						"You are not launch owner."
				);
			}
		}
	}

	/**
	 * Validate {@link Launch#status}
	 *
	 * @param launch               {@link Launch}
	 * @param providedStatus       {@link StatusEnum} launch status from {@link FinishExecutionRQ}
	 * @param fromStatisticsStatus {@link StatusEnum} identified launch status
	 */
	private void validateProvidedStatus(Launch launch, StatusEnum providedStatus, StatusEnum fromStatisticsStatus) {
		/* Validate provided status */
		expect(providedStatus, not(statusIn(IN_PROGRESS, SKIPPED))).verify(INCORRECT_FINISH_STATUS,
				formattedSupplier("Cannot finish launch '{}' with status '{}'", launch.getId(), providedStatus)
		);
		if (PASSED.equals(providedStatus)) {
			/* Validate actual launch status */
			expect(launch.getStatus(), statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS,
					formattedSupplier("Cannot finish launch '{}' with current status '{}' as 'PASSED'", launch.getId(), launch.getStatus())
			);
			expect(fromStatisticsStatus, statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS, formattedSupplier(
					"Cannot finish launch '{}' with calculated automatically status '{}' as 'PASSED'",
					launch.getId(),
					fromStatisticsStatus
			));
		}
	}
}
