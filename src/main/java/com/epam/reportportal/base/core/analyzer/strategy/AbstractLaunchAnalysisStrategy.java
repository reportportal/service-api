/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.analyzer.strategy;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;

/**
 * Common validation and project loading for analysis strategies.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractLaunchAnalysisStrategy implements LaunchAnalysisStrategy {

  protected final ProjectRepository projectRepository;
  protected final LaunchRepository launchRepository;

  /**
   * Supplies project and launch repositories.
   *
   * @param projectRepository project data access
   * @param launchRepository  launch data access
   */
  protected AbstractLaunchAnalysisStrategy(ProjectRepository projectRepository,
      LaunchRepository launchRepository) {
    this.projectRepository = projectRepository;
    this.launchRepository = launchRepository;
  }

  /**
   * Verifies the launch belongs to the membership and is not a debug launch.
   *
   * @param launch            launch to validate
   * @param membershipDetails current membership
   */
  protected void validateLaunch(Launch launch, MembershipDetails membershipDetails) {

    expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId())).verify(
        FORBIDDEN_OPERATION,
        Suppliers.formattedSupplier("Launch with ID '{}' is not under '{}' project.",
            launch.getId(),
            membershipDetails.getProjectName()
        )
    );

    /* Do not process debug launches */
    expect(launch.getMode(), equalTo(LaunchModeEnum.DEFAULT)).verify(INCORRECT_REQUEST,
        "Cannot analyze launches in debug mode.");

  }
}
