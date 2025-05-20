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

package com.epam.ta.reportportal.auth.permissions;


import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("organizationManagerPermission")
@LookupPermission({"organizationManager"})
public class OrganizationManagerPermission implements Permission {

  private final OrganizationUserRepository organizationUserRepository;

  @Autowired
  OrganizationManagerPermission(OrganizationUserRepository organizationUserRepository) {
    this.organizationUserRepository = organizationUserRepository;
  }

  @Override
  public boolean isAllowed(Authentication authentication, Object orgId) {
    if (!authentication.isAuthenticated()) {
      return false;
    }

    ReportPortalUser rpUser = (ReportPortalUser) authentication.getPrincipal();

    BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    var ou = organizationUserRepository.findByUserIdAndOrganization_Id(rpUser.getUserId(),
        (Long) orgId);

    BusinessRule.expect(ou.isPresent(), Predicate.isEqual(true))
        .verify(ErrorType.ACCESS_DENIED);

    return ou.get().getOrganizationRole().equals(OrganizationRole.MANAGER);

  }

}
