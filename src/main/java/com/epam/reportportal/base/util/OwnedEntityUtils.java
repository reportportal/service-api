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
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.DashboardWidget;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for operations with {@link OwnedEntity}
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
public final class OwnedEntityUtils {

  public static final String RESTRICTED_MESSAGE = "Action is not permitted for your role.";
  public static final String LOCK_BYPASS_HINT =
      "Only its owner, an organization manager or an administrator can change it.";
  public static final String DASHBOARD_LOCKED_MESSAGE = "Dashboard is locked. " + LOCK_BYPASS_HINT;
  public static final String WIDGET_LOCKED_MESSAGE = "Widget is on a locked dashboard. Only that dashboard's owner, an organization manager or an administrator can change it.";
  public static final String ENTITY_LOCKED_MESSAGE = "Entity is locked. " + LOCK_BYPASS_HINT;

  private OwnedEntityUtils() {
    //static only
  }

  /**
   * Ensures the user is allowed to modify, delete, lock or unlock the given entity if it is locked. A locked entity can
   * be changed only by its owner, by an organization MANAGER of the project's organization or by an instance
   * ADMINISTRATOR. Unlocked entities are not restricted by this check.
   *
   * @throws ReportPortalException if the entity is locked and the user lacks bypass privileges
   */
  public static void validateOwnedEntityLocked(OwnedEntity entity, MembershipDetails projectDetails, ReportPortalUser user) {
    if (!Boolean.TRUE.equals(entity.getLocked())) {
      return;
    }
    if (canBypassLock(entity, projectDetails, user)) {
      return;
    }
    throw new ReportPortalException(ErrorType.ACCESS_DENIED, resolveLockMessage(entity));
  }

  /**
   * Ensures the user may change or delete the widget. A widget has no lock of its own: it is locked while at least one
   * locked dashboard holds it. Then it can be changed only by an instance ADMINISTRATOR, an organization MANAGER, or a
   * user who owns every locked dashboard holding the widget. The widget's own owner gets no rights from that alone.
   * A widget held by no locked dashboard is treated as not locked, whatever its stale flag says.
   *
   * @throws ReportPortalException if a locked dashboard holds the widget and the user lacks privileges
   */
  public static void validateWidgetLocked(Widget widget, MembershipDetails projectDetails, ReportPortalUser user) {
    List<Dashboard> lockedDashboards = lockedDashboardsOf(widget, null);
    if (lockedDashboards.isEmpty()) {
      return;
    }
    if (UserRole.ADMINISTRATOR.equals(user.getUserRole()) || isOrgManager(projectDetails)) {
      return;
    }
    boolean ownsAll = lockedDashboards.stream()
        .allMatch(d -> d.getOwner() != null && Objects.equals(d.getOwner(), user.getUsername()));
    if (!ownsAll) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, WIDGET_LOCKED_MESSAGE);
    }
  }

  /**
   * Whether the widget is held by a locked dashboard other than the given one.
   */
  public static boolean isHeldByOtherLockedDashboard(Widget widget, Long excludedDashboardId) {
    return !lockedDashboardsOf(widget, excludedDashboardId).isEmpty();
  }

  private static List<Dashboard> lockedDashboardsOf(Widget widget, Long excludedDashboardId) {
    return widget.getDashboardWidgets().stream()
        .map(DashboardWidget::getDashboard)
        .filter(d -> Boolean.TRUE.equals(d.getLocked()))
        .filter(d -> excludedDashboardId == null || !Objects.equals(d.getId(), excludedDashboardId))
        .toList();
  }

  private static boolean isOrgManager(MembershipDetails details) {
    OrganizationRole role = details.getOrgRole();
    return role != null && role.sameOrHigherThan(OrganizationRole.MANAGER);
  }

  private static boolean canBypassLock(OwnedEntity entity, MembershipDetails details, ReportPortalUser user) {
    if (UserRole.ADMINISTRATOR.equals(user.getUserRole())) {
      return true;
    }
    if (entity.getOwner() != null && Objects.equals(entity.getOwner(), user.getUsername())) {
      return true;
    }
    return isOrgManager(details);
  }

  private static String resolveLockMessage(OwnedEntity entity) {
    return switch (entity) {
      case Dashboard _ -> DASHBOARD_LOCKED_MESSAGE;
      case Widget _ -> WIDGET_LOCKED_MESSAGE;
      default -> ENTITY_LOCKED_MESSAGE;
    };
  }
}
