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

package com.epam.ta.reportportal.core.item.impl;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.reportportal.rules.exception.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.user.UserRole;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchAccessValidatorImpl implements LaunchAccessValidator {

  private final LaunchRepository launchRepository;

  public LaunchAccessValidatorImpl(LaunchRepository launchRepository) {
    this.launchRepository = launchRepository;
  }

  @Override
  //TODO separate project validation from launch state validation (mode)
  public void validate(Launch launch, MembershipDetails membershipDetails,
      ReportPortalUser user) {
    if (user.getUserRole() != UserRole.ADMINISTRATOR) {
      expect(launch.getProjectId(), equalTo(membershipDetails.getProjectId())).verify(
          FORBIDDEN_OPERATION,
          formattedSupplier(
              "Specified launch with id '{}' not referenced to specified project with id '{}'",
              launch.getId(),
              membershipDetails.getProjectId()
          )
      );
    }
  }

  @Override
  public void validate(Long launchId, MembershipDetails membershipDetails,
      ReportPortalUser user) {
    Launch launch = launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
    validate(launch, membershipDetails, user);
  }

}
