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
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishForcedEvent;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.LaunchWithLinkRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.FinishLaunchHandler}
 *
 * @author Pave Bortnik
 */
@Service
@Transactional
public class FinishLaunchHandler implements com.epam.ta.reportportal.core.launch.FinishLaunchHandler {

	private static final String LAUNCH_STOP_DESCRIPTION = " stopped";
	private static final String LAUNCH_STOP_TAG = "stopped";

	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final MessageBus messageBus;
	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public FinishLaunchHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository, MessageBus messageBus,
			ApplicationEventPublisher eventPublisher) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.messageBus = messageBus;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public LaunchWithLinkRS finishLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ,
			ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));

		validateRoles(launch, user, projectDetails);
		validate(launch, finishLaunchRQ);

		if (testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)) {
			testItemRepository.interruptInProgressItems(launchId);
		}

		launch = new LaunchBuilder(launch).addDescription(finishLaunchRQ.getDescription())
				.addTags(finishLaunchRQ.getTags())
				.addEndTime(finishLaunchRQ.getEndTime())
				.get();

		Optional<StatusEnum> statusEnum = fromValue(finishLaunchRQ.getStatus());
		StatusEnum fromStatisticsStatus = PASSED;
		if (launchRepository.identifyStatus(launchId)) {
			fromStatisticsStatus = StatusEnum.FAILED;
		}
		if (statusEnum.isPresent()) {
			validateProvidedStatus(launch, statusEnum.get(), fromStatisticsStatus);
		}
		launch.setStatus(statusEnum.orElse(fromStatisticsStatus));
		messageBus.publishActivity(new LaunchFinishedEvent(launch));

		LaunchWithLinkRS rs = new LaunchWithLinkRS();
		rs.setId(launchId);
		rs.setNumber(launch.getNumber());
		return rs;
	}

	@Override
	public OperationCompletionRS stopLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

		validateRoles(launch, user, projectDetails);

		expect(launch.getStatus(), statusIn(IN_PROGRESS)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		launch = new LaunchBuilder(launch).addDescription(ofNullable(launch.getDescription()).orElse("").concat(LAUNCH_STOP_DESCRIPTION))
				.addTag(LAUNCH_STOP_TAG)
				.addStatus(ofNullable(finishLaunchRQ.getStatus()).orElse(STOPPED.name()))
				.get();
		launch.setEndTime(LocalDateTime.now());

		launchRepository.save(launch);
		testItemRepository.interruptInProgressItems(launchId);

		messageBus.publishActivity(new LaunchFinishForcedEvent(launch, user.getUserId()));
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
				launch.getStartTime(),
				launch.getId()
		);
	}

	/**
	 * Validate {@link ReportPortalUser} credentials and {@link Launch} affiliation to the {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param launch         {@link Launch}
	 * @param user           {@link ReportPortalUser}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 */
	private void validateRoles(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(launch.getUser().getLogin(), user.getUsername())) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			expect(projectDetails.getProjectRole(), equalTo(PROJECT_MANAGER)).verify(ACCESS_DENIED);
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
