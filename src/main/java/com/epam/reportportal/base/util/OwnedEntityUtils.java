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

package com.epam.reportportal.base.util;


import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;

/**
 * Utility class for operations with {@link OwnedEntity}
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
public final class OwnedEntityUtils {

  public static final String RESTRICTED_MESSAGE = "Action is not permitted for your role.";
  public static final String DASHBOARD_LOCKED_MESSAGE = "Dashboard is locked. " + RESTRICTED_MESSAGE;
  public static final String WIDGET_LOCKED_MESSAGE = "Widget is used in a locked dashboard. " + RESTRICTED_MESSAGE;

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
  public static void validateOwnedEntityLocked(OwnedEntity entity, MembershipDetails projectDetails,
      ReportPortalUser user) {
    if (!user.getUserRole().equals(UserRole.ADMINISTRATOR)
        && projectDetails.getProjectRole().lowerThan(ProjectRole.EDITOR)
        && Boolean.TRUE.equals(entity.getLocked())) {
      String message = switch (entity) {
        case Dashboard _ -> DASHBOARD_LOCKED_MESSAGE;
        case Widget _ -> WIDGET_LOCKED_MESSAGE;
        default -> RESTRICTED_MESSAGE;
      };
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, message);
    }
  }
}
