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
import com.epam.ta.reportportal.commons.ReportPortalUser.OrganizationDetails;
import com.epam.ta.reportportal.commons.ReportPortalUser.OrganizationDetails.ProjectDetails;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component("organizationManagerPermission")
@LookupPermission({"organizationManager"})
public class OrganizationManagerPermission implements Permission {

  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;


  @Autowired
  OrganizationManagerPermission(OrganizationUserRepository organizationUserRepository,
      OrganizationRepositoryCustom organizationRepositoryCustom) {
    this.organizationUserRepository = organizationUserRepository;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
  }

  @Override
  public boolean isAllowed(Authentication authentication, Object orgId) {
    if (!authentication.isAuthenticated()) {
      return false;
    }

    OAuth2Authentication oauth = (OAuth2Authentication) authentication;
    ReportPortalUser rpUser = (ReportPortalUser) oauth.getUserAuthentication().getPrincipal();
    BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    var org = organizationRepositoryCustom.findById((Long) orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var ou = organizationUserRepository.findByUserIdAndOrganization_Id(rpUser.getUserId(),
        (Long) orgId);

    BusinessRule.expect(ou.isPresent(), Predicate.isEqual(true))
        .verify(ErrorType.ACCESS_DENIED);

    return ou.get().getOrganizationRole().equals(OrganizationRole.MANAGER);

  }

  private void fillProjectDetails(ReportPortalUser rpUser, String resolvedProjectName,
      MembershipDetails membershipDetails) {
    final Map<String, OrganizationDetails> organizationDetails = HashMap.newHashMap(2);

    var prjDetailsMap = new HashMap<String, ProjectDetails>();

    var prjDetails = new ProjectDetails(membershipDetails.getProjectId(),
        membershipDetails.getProjectName(),
        membershipDetails.getProjectKey(),
        membershipDetails.getProjectRole(),
        membershipDetails.getOrgId());
    prjDetailsMap.put(membershipDetails.getProjectKey(), prjDetails);

    var od = new OrganizationDetails(
        membershipDetails.getOrgId(),
        membershipDetails.getOrgName(),
        membershipDetails.getOrgRole(),
        prjDetailsMap);

    organizationDetails.put(resolvedProjectName, od);
    rpUser.setOrganizationDetails(organizationDetails);
  }
}