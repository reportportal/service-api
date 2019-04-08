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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.util.LaunchLinkGenerator;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.FinishLaunchRS;

import java.util.List;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;
import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.PASSED;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.SKIPPED;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.ws.model.ErrorType.FINISH_LAUNCH_NOT_ALLOWED;
import static com.epam.ta.reportportal.ws.model.ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_FINISH_STATUS;

/**
 * {@link FinishExecutionRQ} request handler
 *
 * @author Andrei_Kliashchonak
 */

public interface FinishLaunchHandler {

	Launch finishLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Updates {@link Launch} instance
	 *
	 * @param launchId       ID of launch
	 * @param finishLaunchRQ Request data
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return FinishLaunchRS
	 */
	FinishLaunchRS finishLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, LaunchLinkGenerator.LinkParams linkParams);

	/**
	 * Stop Launch instance by user
	 *
	 * @param launchId       ID of launch
	 * @param finishLaunchRQ Request data
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS stopLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Bulk stop launches operation.
	 *
	 * @param bulkRQ         Bulk request
	 * @param projectDetails Project Details
	 * @param user           User
	 * @return OperationCompletionsRS
	 */
	List<OperationCompletionRS> stopLaunch(BulkRQ<String, FinishExecutionRQ> bulkRQ, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

	/**
	 * Validate {@link Launch#status} and value of the {@link Launch#endTime}
	 *
	 * @param launch            {@link Launch}
	 * @param finishExecutionRQ {@link FinishExecutionRQ}
	 */
	default void validate(Launch launch, FinishExecutionRQ finishExecutionRQ) {
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
	default void validateRoles(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
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
	default void validateProvidedStatus(Launch launch, StatusEnum providedStatus, StatusEnum fromStatisticsStatus) {
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