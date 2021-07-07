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

package com.epam.ta.reportportal.core.launch.util;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.Preconditions.statusIn;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class LaunchValidator {

	private LaunchValidator() {
		//static only
	}

	/**
	 * Validate {@link Launch#status} and value of the {@link Launch#endTime}
	 *
	 * @param launch            {@link Launch}
	 * @param finishExecutionRQ {@link FinishExecutionRQ}
	 */
	public static void validate(Launch launch, FinishExecutionRQ finishExecutionRQ) {
		expect(launch.getStatus(), equalTo(IN_PROGRESS)).verify(FINISH_LAUNCH_NOT_ALLOWED,
				formattedSupplier("Launch '{}' already finished with status '{}'", launch.getId(), launch.getStatus())
		);

		expect(finishExecutionRQ.getEndTime(), Preconditions.sameTimeOrLater(launch.getStartTime())).verify(
				FINISH_TIME_EARLIER_THAN_START_TIME,
				TO_LOCAL_DATE_TIME.apply(finishExecutionRQ.getEndTime()),
				launch.getStartTime(),
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
	public static void validateRoles(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			if (!launch.isRerun() && projectDetails.getProjectRole().lowerThan(PROJECT_MANAGER)) {
				expect(user.getUserId(), Predicate.isEqual(launch.getUserId())).verify(ACCESS_DENIED, "You are not launch owner.");
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
	public static void validateProvidedStatus(Launch launch, StatusEnum providedStatus, StatusEnum fromStatisticsStatus) {
		/* Validate provided status */
		expect(providedStatus, not(statusIn(IN_PROGRESS, SKIPPED, UNTESTED))).verify(INCORRECT_FINISH_STATUS,
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
