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

package com.epam.reportportal.base.auth.permissions;

import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole.MANAGER;
import static java.util.Objects.isNull;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.GroupRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.Group;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("groupEditorPermission")
@LookupPermission({"groupEditor"})
public class GroupEditorPermission implements Permission {

  private final GroupRepository groupRepository;
  private final OrganizationUserRepository organizationUserRepository;

  @Autowired
  public GroupEditorPermission(
      GroupRepository groupRepository,
      OrganizationUserRepository organizationUserRepository
  ) {
    this.groupRepository = groupRepository;
    this.organizationUserRepository = organizationUserRepository;
  }

  @Override
  public boolean isAllowed(Authentication authentication, Object groupId) {
    if (!authentication.isAuthenticated()) {
      return false;
    }

    var rpUser = (ReportPortalUser) authentication.getPrincipal();
    BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    var orgId = groupRepository.findById((Long) groupId)
        .filter(group -> !isNull(group.getOrganizationId()))
        .map(Group::getOrganizationId)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.NOT_FOUND, "Organization group with id " + groupId
        ));

    return organizationUserRepository
        .findByUserIdAndOrganization_Id(rpUser.getUserId(), orgId)
        .map(OrganizationUser::getOrganizationRole)
        .filter(MANAGER::sameOrHigherThan)
        .isPresent();
  }
}
