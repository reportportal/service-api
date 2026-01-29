/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.util;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.OwnedEntity;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.widget.Widget;

/**
 * Utility class for operations with {@link OwnedEntity}
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
public final class OwnedEntityUtils {

  public static final String RESTRICTED_MESSAGE = "Action is not permitted for your role.";
  public static final String DASHBOARD_LOCKED_MESSAGE = "Dashboard is locked. " + RESTRICTED_MESSAGE;
  public static final String WIDGET_LOCKED_MESSAGE = "Widget is used in a locked dashboard. " + RESTRICTED_MESSAGE;
  public static final String FILTER_LOCKED_MESSAGE = "Filter is used in a locked dashboard. " + RESTRICTED_MESSAGE;

  private OwnedEntityUtils() {
    //static only
  }

  /**
   * Validates if a user has permissions to modify a locked entity.
   *
   * @param entity         The entity to validate.
   * @param projectDetails The project details.
   * @param user           The user.
   * @throws ReportPortalException if the user does not have permissions.
   */
  public static void validateOwnedEntityLocked(OwnedEntity entity, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user) {
    if (!user.getUserRole().equals(UserRole.ADMINISTRATOR)
        && projectDetails.getProjectRole().lowerThan(ProjectRole.PROJECT_MANAGER)
        && entity.getLocked()) {
      String message = switch (entity) {
        case Dashboard dashboard -> DASHBOARD_LOCKED_MESSAGE;
        case Widget widget -> WIDGET_LOCKED_MESSAGE;
        case UserFilter userFilter -> FILTER_LOCKED_MESSAGE;
        default -> RESTRICTED_MESSAGE;
      };
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, message);
    }
  }
}
