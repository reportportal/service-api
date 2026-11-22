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

package com.epam.reportportal.base.util;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrganizationUserValidatorTest {

  @Test
  void personalOwnerCannotBeRemoved() {
    var org = new Organization();
    org.setOrganizationType(OrganizationType.PERSONAL);
    org.setOwnerId(7L);
    assertThrows(ReportPortalException.class,
        () -> OrganizationUserValidator.validatePersonalOrganizationOwnerKept(org, List.of(7L)));
    assertDoesNotThrow(() -> OrganizationUserValidator.validatePersonalOrganizationOwnerKept(org, List.of(8L)));
  }

  @Test
  void managerCannotDemoteSelfButAdminCan() {
    var manager = getRpUser("m", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L);
    var admin = getRpUser("a", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR, 1L);
    assertThrows(ReportPortalException.class, () -> OrganizationUserValidator.validateManagerChangingRole(manager,
        manager.getUserId(), OrganizationRole.MANAGER, OrganizationRole.MEMBER));
    assertDoesNotThrow(() -> OrganizationUserValidator.validateManagerChangingRole(admin, admin.getUserId(),
        OrganizationRole.MANAGER, OrganizationRole.MEMBER));
    assertDoesNotThrow(() -> OrganizationUserValidator.validateManagerChangingRole(manager, 99L,
        OrganizationRole.MANAGER, OrganizationRole.MEMBER));
  }
}
