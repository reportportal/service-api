/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.store.commons.Predicates.not;
import static com.epam.ta.reportportal.store.database.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.store.database.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link IFinishLaunchHandler}
 *
 * @author Pave Bortnik
 */
@Service
@Transactional
public class FinishLaunchHandler implements IFinishLaunchHandler {

	private static final String LAUNCH_STOP_DESCRIPTION = " stopped";
	private static final String LAUNCH_STOP_TAG = "stopped";

	private LaunchRepository launchRepository;

	private TestItemRepository testItemRepository;

	private ApplicationEventPublisher eventPublisher;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public OperationCompletionRS finishLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, String projectName, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));

		validateRoles(launch, user, projectName);
		validate(launch, finishLaunchRQ);

		launch = new LaunchBuilder(launch).addDescription(finishLaunchRQ.getDescription())
				.addTags(finishLaunchRQ.getTags())
				.addEndTime(finishLaunchRQ.getEndTime())
				.get();

		Optional<StatusEnum> statusEnum = fromValue(finishLaunchRQ.getStatus());
		StatusEnum fromStatistics = PASSED;
		if (launchRepository.identifyStatus(launchId)) {
			fromStatistics = StatusEnum.FAILED;
		}
		StatusEnum fromStatisticsStatus = fromStatistics;
		if (statusEnum.isPresent()) {
			validateProvidedStatus(launch, statusEnum.get(), fromStatisticsStatus);
		}
		launch.setStatus(statusEnum.orElse(fromStatistics));
		launchRepository.save(launch);
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully finished.");
	}

	@Override
	public OperationCompletionRS stopLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ, String projectName, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

		validateRoles(launch, user, projectName);

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

		//		eventPublisher.publishEvent(new LaunchFinishForcedEvent(launch, userName));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully stopped.");
	}

	@Override
	public List<OperationCompletionRS> stopLaunch(BulkRQ<FinishExecutionRQ> bulkRQ, String projectName, ReportPortalUser user) {
		return bulkRQ.getEntities()
				.entrySet()
				.stream()
				.map(entry -> stopLaunch(entry.getKey(), entry.getValue(), projectName, user))
				.collect(toList());
	}

	private void validate(Launch launch, FinishExecutionRQ finishExecutionRQ) {
		expect(launch.getStatus(), equalTo(IN_PROGRESS)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		expect(finishExecutionRQ.getEndTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(
				FINISH_TIME_EARLIER_THAN_START_TIME, finishExecutionRQ.getEndTime(), launch.getStartTime(), launch.getId());

		List<TestItem> items = testItemRepository.selectItemsInStatusByLaunch(launch.getId(), IN_PROGRESS);
		expect(items.isEmpty(), equalTo(true)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' has items '[{}]' with '{}' status", launch.getId().toString(),
						items.stream().map(it -> it.getItemId().toString()).collect(Collectors.joining(",")), IN_PROGRESS.name()
				)
		);
	}

	private void validateRoles(Launch launch, ReportPortalUser user, String projectName) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(launch.getUserId(), user.getUserId())) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			expect(projectDetails.getProjectRole(), equalTo(PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}
	}

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
			expect(fromStatisticsStatus, statusIn(IN_PROGRESS, PASSED)).verify(INCORRECT_FINISH_STATUS,
					formattedSupplier("Cannot finish launch '{}' with calculated automatically status '{}' as 'PASSED'", launch.getId(),
							fromStatisticsStatus
					)
			);
		}
	}
}
