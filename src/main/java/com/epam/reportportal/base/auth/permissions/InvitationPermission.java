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

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.isNull;
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.not;
import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole.MANAGER;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole.VIEWER;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static java.util.function.Predicate.isEqual;

import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser.OrganizationDetails;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("invitationPermission")
@LookupPermission({"invitationAllowed"})
public class InvitationPermission implements Permission {

  private final OrganizationUserRepository organizationUserRepository;

  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final ProjectRepository projectRepository;

  @Autowired
  InvitationPermission(OrganizationUserRepository organizationUserRepository,
      OrganizationRepositoryCustom organizationRepositoryCustom,
      ProjectRepository projectRepository) {
    this.organizationUserRepository = organizationUserRepository;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.projectRepository = projectRepository;
  }

  @Override
  public boolean isAllowed(Authentication authentication, Object invitationRequest) {
    if (!authentication.isAuthenticated()) {
      return false;
    }
    ReportPortalUser rpUser = (ReportPortalUser) authentication.getPrincipal();
    BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    List<InvitationRequestOrganizationsInner> orgs =
        ((InvitationRequest) invitationRequest).getOrganizations();

    expect(CollectionUtils.isEmpty(orgs), isEqual(false))
        .verify(ACCESS_DENIED, "Only administrators allowed to invite users on instance level");

    rpUser.setOrganizationDetails(new HashMap<>());
    orgs.forEach(orgInfo -> checkOrganizationAccess(rpUser, orgInfo));

    return true;

  }

  private void checkOrganizationAccess(ReportPortalUser rpUser,
      InvitationRequestOrganizationsInner orgInfo) {

    var org = organizationRepositoryCustom.findById(orgInfo.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND,
            orgInfo.getId()));

    var orgUser = organizationUserRepository
        .findByUserIdAndOrganization_Id(rpUser.getUserId(), org.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
            "You are not assigned to organization"));

    fillOrganizationDetails(rpUser, org, orgUser);

    if (CollectionUtils.isEmpty(orgInfo.getProjects())) {
      expect(orgUser.getOrganizationRole().sameOrHigherThan(MANAGER), isEqual(true))
          .verify(ACCESS_DENIED, "You are not manager of the organization");
    }

    expect(orgUser.getOrganizationRole()
        .sameOrHigherThan(OrganizationRole.valueOf(orgInfo.getOrgRole().name())), isEqual(true))
        .verify(ACCESS_DENIED);

    orgInfo.getProjects()
        .forEach(assigningPrj -> checkProjectAccess(rpUser, orgUser, assigningPrj));

  }

  private void checkProjectAccess(ReportPortalUser rpUser, OrganizationUser orgUser,
      UserProjectInfo assigningPrj) {

    Project prj = projectRepository.findById(assigningPrj.getId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND,
            assigningPrj.getId()));
    if (orgUser.getOrganizationRole().equals(OrganizationRole.MEMBER)) {

      ProjectUser projectUser = findUserConfigByLogin(prj, rpUser.getUsername());
      expect(projectUser, not(isNull()))
          .verify(ACCESS_DENIED, formattedSupplier("'{}' is not your project", prj.getId()));

      if (VIEWER.equals(projectUser.getProjectRole())) {
        throw new ReportPortalException(ErrorType.ACCESS_DENIED,
            Suppliers.formattedSupplier("You don't have permissions to invite users on the project '{}'",
                prj.getId()).get());
      }
    }

  }


  private void fillOrganizationDetails(ReportPortalUser rpUser, Organization organization,
      OrganizationUser orgUser) {

    var orgDetails = new OrganizationDetails(
        organization.getId(),
        organization.getName(),
        orgUser.getOrganizationRole(),
        new HashMap<>()
    );
    rpUser.getOrganizationDetails()
        .put(organization.getId().toString(), orgDetails);
  }

}
