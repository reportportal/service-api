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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface LaunchAccessValidator {

  /**
   * @param launch         {@link com.epam.ta.reportportal.entity.launch.Launch}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   */
  void validate(Launch launch, MembershipDetails membershipDetails,
      ReportPortalUser user);

  /**
   * @param launchId       {@link com.epam.ta.reportportal.entity.launch.Launch#getId()}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   */
  void validate(Long launchId, MembershipDetails membershipDetails,
      ReportPortalUser user);
}
