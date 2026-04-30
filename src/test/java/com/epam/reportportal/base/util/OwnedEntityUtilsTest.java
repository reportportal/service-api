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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.OwnedEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
class OwnedEntityUtilsTest {

  private ReportPortalUser adminUser;
  private MembershipDetails adminProjectDetails;

  private ReportPortalUser projectManagerUser;
  private MembershipDetails projectManagerProjectDetails;

  private ReportPortalUser memberUser;
  private MembershipDetails memberProjectDetails;

  @BeforeEach
  void setUp() {
    adminUser = mock(ReportPortalUser.class);
    adminProjectDetails = mock(MembershipDetails.class);
    when(adminUser.getUserRole()).thenReturn(UserRole.ADMINISTRATOR);

    projectManagerUser = mock(ReportPortalUser.class);
    projectManagerProjectDetails = mock(MembershipDetails.class);
    when(projectManagerUser.getUserRole()).thenReturn(UserRole.USER);
    when(projectManagerProjectDetails.getProjectRole()).thenReturn(ProjectRole.EDITOR);

    memberUser = mock(ReportPortalUser.class);
    memberProjectDetails = mock(MembershipDetails.class);
    when(memberUser.getUserRole()).thenReturn(UserRole.USER);
    when(memberProjectDetails.getProjectRole()).thenReturn(ProjectRole.EDITOR);
  }

  @Test
  void validateOwnedEntityLockedShouldNotThrowForAdmin() {
    OwnedEntity lockedEntity = mock(Dashboard.class);
    when(lockedEntity.getLocked()).thenReturn(true);
    assertDoesNotThrow(() -> OwnedEntityUtils.validateOwnedEntityLocked(lockedEntity, adminProjectDetails, adminUser));
  }

  @Test
  void validateOwnedEntityLockedShouldNotThrowForProjectManager() {
    OwnedEntity lockedEntity = mock(Dashboard.class);
    when(lockedEntity.getLocked()).thenReturn(true);
    assertDoesNotThrow(() -> OwnedEntityUtils.validateOwnedEntityLocked(lockedEntity, projectManagerProjectDetails,
        projectManagerUser));
  }

  @Test
  void validateOwnedEntityLockedShouldNotThrowWhenNotLocked() {
    OwnedEntity notLockedEntity = mock(Dashboard.class);
    when(notLockedEntity.getLocked()).thenReturn(false);
    assertDoesNotThrow(
        () -> OwnedEntityUtils.validateOwnedEntityLocked(notLockedEntity, memberProjectDetails, memberUser));
  }

  @Test
  void validateOwnedEntityLockedShouldThrowForDashboard() {
    Dashboard lockedDashboard = mock(Dashboard.class);
    when(lockedDashboard.getLocked()).thenReturn(true);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> OwnedEntityUtils.validateOwnedEntityLocked(lockedDashboard, memberProjectDetails, memberUser));

    assertTrue(exception.getMessage().contains(DASHBOARD_LOCKED_MESSAGE));
  }

  @Test
  void validateOwnedEntityLockedShouldThrowForWidget() {
    Widget lockedWidget = mock(Widget.class);
    when(lockedWidget.getLocked()).thenReturn(true);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> OwnedEntityUtils.validateOwnedEntityLocked(lockedWidget, memberProjectDetails, memberUser));

    assertTrue(exception.getMessage().contains(WIDGET_LOCKED_MESSAGE));

  }

  @Test
  void validateOwnedEntityLockedShouldThrowForGenericEntity() {
    OwnedEntity lockedEntity = mock(OwnedEntity.class);
    when(lockedEntity.getLocked()).thenReturn(true);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> OwnedEntityUtils.validateOwnedEntityLocked(lockedEntity, memberProjectDetails, memberUser));

    assertTrue(exception.getMessage().contains(RESTRICTED_MESSAGE));
  }
}
