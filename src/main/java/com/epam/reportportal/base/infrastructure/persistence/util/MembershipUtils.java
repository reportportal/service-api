/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.util;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser.OrganizationDetails;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser.OrganizationDetails.ProjectDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;

public class MembershipUtils {

  private MembershipUtils() {
  }

  public static MembershipDetails rpUserToMembership(ReportPortalUser user) {
    OrganizationDetails od = user.getOrganizationDetails()
        .values().stream()
        .findFirst()
        .orElseThrow();
    return orgDetailsToMembership(od);
  }

  public static MembershipDetails orgDetailsToMembership(OrganizationDetails od) {
    ProjectDetails projectDetails = od.getProjectDetails()
        .values().stream()
        .findFirst()
        .orElse(new ProjectDetails());

    MembershipDetails md = new MembershipDetails();
    md.setOrgId(od.getOrgId());
    md.setOrgRole(od.getOrgRole());
    md.setOrgName(od.getOrgName());
    md.setProjectId(projectDetails.getProjectId());
    md.setProjectName(projectDetails.getProjectName());
    md.setProjectKey(projectDetails.getProjectKey());
    md.setProjectRole(projectDetails.getProjectRole());
    return md;
  }

}
