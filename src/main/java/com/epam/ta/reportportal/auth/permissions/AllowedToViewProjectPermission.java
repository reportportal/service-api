/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Check whether user is organization manager or assigned to project
 *
 * @author Andrei Varabyeu
 */
@Component("allowedToViewProjectPermission")
@LookupPermission({"allowedToViewProject"})
class AllowedToViewProjectPermission extends BaseProjectPermission {

  @Autowired
  AllowedToViewProjectPermission(ProjectExtractor projectExtractor) {
    super(projectExtractor);
  }

  @Override
  public boolean checkAllowed(ReportPortalUser user, MembershipDetails membershipDetails) {
    return OrganizationRole.MANAGER == membershipDetails.getOrgRole()
        || membershipDetails.getProjectRole() != null;
  }
}
