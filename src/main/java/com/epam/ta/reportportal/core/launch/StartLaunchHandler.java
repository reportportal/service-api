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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRS;
import java.util.function.Predicate;

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
   * @param projectDetails Project Details
   * @param startLaunchRQ  Request Data
   * @return StartLaunchRS
   */
  StartLaunchRS startLaunch(ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails,
      StartLaunchRQ startLaunchRQ);

  /**
   * Validate {@link ReportPortalUser} credentials. User with a {@link ProjectRole#CUSTOMER} role
   * can't report launches in a debug mode.
   *
   * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
   * @param startLaunchRQ  {@link StartLaunchRQ}
   */
  default void validateRoles(ReportPortalUser.ProjectDetails projectDetails,
      StartLaunchRQ startLaunchRQ) {
    expect(
        Mode.DEBUG.equals(startLaunchRQ.getMode()) && ProjectRole.CUSTOMER.equals(
            projectDetails.getProjectRole()),
        Predicate.isEqual(false)
    ).verify(ErrorType.FORBIDDEN_OPERATION);
  }
}