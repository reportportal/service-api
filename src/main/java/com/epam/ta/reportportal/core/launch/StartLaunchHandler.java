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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRS;

/**
 * Start Launch operation handler
 *
 * @author Andrei Varabyeu
 */
public interface StartLaunchHandler {

  /**
   * Creates new launch for specified project
   *
   * @param user           ReportPortal user
   * @param membershipDetails Membership details
   * @param startLaunchRQ  Request Data
   * @return StartLaunchRS
   */
  StartLaunchRS startLaunch(ReportPortalUser user, MembershipDetails membershipDetails,
      StartLaunchRQ startLaunchRQ);

  /**
   * Validate {@link ReportPortalUser} credentials.
   *
   * @param membershipDetails {@link MembershipDetails}
   * @param startLaunchRQ  {@link StartLaunchRQ}
   */
  default void validateRoles(MembershipDetails membershipDetails,
      StartLaunchRQ startLaunchRQ) {
  }
}
