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

package com.epam.reportportal.base.core.launch;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.reporting.LaunchResource;
import com.epam.reportportal.base.reporting.MergeLaunchesRQ;

/**
 * Merge launches handler in common one
 *
 * @author Pavel_Bortnik
 */
public interface MergeLaunchHandler {

  /**
   * Merges launches.
   *
   * @param membershipDetails Membership details
   * @param user              User
   * @param mergeLaunchesRQ   Request data
   * @return OperationCompletionsRS
   */
  LaunchResource mergeLaunches(MembershipDetails membershipDetails,
      ReportPortalUser user, MergeLaunchesRQ mergeLaunchesRQ);

}
