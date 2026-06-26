/*
 * Copyright 2023 EPAM Systems
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

import static com.epam.reportportal.base.util.OwnedEntityUtils.DASHBOARD_LOCKED_MESSAGE;
import static com.epam.reportportal.base.util.OwnedEntityUtils.RESTRICTED_MESSAGE;
import static com.epam.reportportal.base.util.OwnedEntityUtils.WIDGET_LOCKED_MESSAGE;
import static com.epam.reportportal.base.util.OwnedEntityUtils.validateOwnedEntityLocked;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
class OwnedEntityUtilsTest {

  @Test
  void validateOwnedEntityLockedWhenEntityNotLockedShouldNotThrow() {
    // Given
    Dashboard entity = mockDashboard(false);

    // When / Then
    assertDoesNotThrow(
        () -> validateOwnedEntityLocked(entity, membership(null, ProjectRole.VIEWER), user(UserRole.USER)));
  }

  @Test
  void validateOwnedEntityLockedWhenLockedIsNullShouldNotThrow() {
    // Given
    Dashboard entity = mock(Dashboard.class);
    when(entity.getLocked()).thenReturn(null);

    // When / Then
    assertDoesNotThrow(
        () -> validateOwnedEntityLocked(entity, membership(null, ProjectRole.VIEWER), user(UserRole.USER)));
  }

  @Test
  void validateOwnedEntityLockedWhenUserIsAdministratorShouldNotThrow() {
    // Given
    Dashboard entity = mockDashboard(true);

    // When / Then
    assertDoesNotThrow(
        () -> validateOwnedEntityLocked(entity, membership(null, ProjectRole.VIEWER), user(UserRole.ADMINISTRATOR)));
  }

  @Test
  void validateOwnedEntityLockedWhenOrgRoleIsManagerShouldNotThrow() {
    // Given
    Dashboard entity = mockDashboard(true);
    MembershipDetails details = membership(OrganizationRole.MANAGER, ProjectRole.VIEWER);

    // When / Then
    assertDoesNotThrow(() -> validateOwnedEntityLocked(entity, details, user(UserRole.USER)));
  }

  @Test
  void validateOwnedEntityLockedWhenProjectRoleIsEditorShouldNotThrow() {
    // Given
    Dashboard entity = mockDashboard(true);
    MembershipDetails details = membership(OrganizationRole.MEMBER, ProjectRole.EDITOR);

    // When / Then
    assertDoesNotThrow(() -> validateOwnedEntityLocked(entity, details, user(UserRole.USER)));
  }

  @Test
  void validateOwnedEntityLockedWhenDashboardLockedAndUserLacksPrivilegesShouldThrowDashboardMessage() {
    // Given
    Dashboard entity = mockDashboard(true);
    MembershipDetails details = membership(OrganizationRole.MEMBER, ProjectRole.VIEWER);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> validateOwnedEntityLocked(entity, details, user(UserRole.USER)));

    // Then
    assertTrue(ex.getMessage().contains(DASHBOARD_LOCKED_MESSAGE));
  }

  @Test
  void validateOwnedEntityLockedWhenWidgetLockedAndUserLacksPrivilegesShouldThrowWidgetMessage() {
    // Given
    Widget entity = mock(Widget.class);
    when(entity.getLocked()).thenReturn(true);
    MembershipDetails details = membership(OrganizationRole.MEMBER, ProjectRole.VIEWER);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> validateOwnedEntityLocked(entity, details, user(UserRole.USER)));

    // Then
    assertTrue(ex.getMessage().contains(WIDGET_LOCKED_MESSAGE));
  }

  @Test
  void validateOwnedEntityLockedWhenGenericEntityLockedShouldThrowRestrictedMessage() {
    // Given
    OwnedEntity entity = mock(OwnedEntity.class);
    when(entity.getLocked()).thenReturn(true);
    MembershipDetails details = membership(OrganizationRole.MEMBER, ProjectRole.VIEWER);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> validateOwnedEntityLocked(entity, details, user(UserRole.USER)));

    // Then
    assertTrue(ex.getMessage().contains(RESTRICTED_MESSAGE));
  }

  @Test
  void validateOwnedEntityLockedWhenAllRolesAreNullShouldThrow() {
    // Given
    Dashboard entity = mockDashboard(true);
    MembershipDetails details = membership(null, null);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> validateOwnedEntityLocked(entity, details, user(UserRole.USER)));

    // Then
    assertTrue(ex.getMessage().contains(DASHBOARD_LOCKED_MESSAGE));
  }

  private static Dashboard mockDashboard(boolean locked) {
    Dashboard dashboard = mock(Dashboard.class);
    when(dashboard.getLocked()).thenReturn(locked);
    return dashboard;
  }

  private static ReportPortalUser user(UserRole role) {
    ReportPortalUser user = mock(ReportPortalUser.class);
    when(user.getUserRole()).thenReturn(role);
    return user;
  }

  private static MembershipDetails membership(OrganizationRole orgRole, ProjectRole projectRole) {
    MembershipDetails details = mock(MembershipDetails.class);
    when(details.getOrgRole()).thenReturn(orgRole);
    when(details.getProjectRole()).thenReturn(projectRole);
    return details;
  }
}
