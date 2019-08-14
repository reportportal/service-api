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

package com.epam.ta.reportportal.core.analyzer.strategy;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractLaunchAnalysisStrategy implements LaunchAnalysisStrategy {

	protected final ProjectRepository projectRepository;
	protected final LaunchRepository launchRepository;

	protected AbstractLaunchAnalysisStrategy(ProjectRepository projectRepository, LaunchRepository launchRepository) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
	}

	protected void validateLaunch(Launch launch, ReportPortalUser.ProjectDetails projectDetails) {

		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				Suppliers.formattedSupplier("Launch with ID '{}' is not under '{}' project.",
						launch.getId(),
						projectDetails.getProjectName()
				)
		);

		/* Do not process debug launches */
		expect(launch.getMode(), equalTo(LaunchModeEnum.DEFAULT)).verify(INCORRECT_REQUEST, "Cannot analyze launches in debug mode.");

	}
}
