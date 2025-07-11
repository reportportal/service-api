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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.OrgUserProject;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import java.util.function.Function;

/**
 * Converts project entity into OrgUserProject api model.
 *
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class OrganizationUserConverter {

  public static Function<MembershipDetails, OrgUserProject> MEMBERSHIP_TO_ORG_USER_PROJECT =
      membershipDetails -> new OrgUserProject()
          .id(membershipDetails.getProjectId())
          .name(membershipDetails.getProjectName())
          .slug(membershipDetails.getProjectSlug())
          .projectRole(ProjectRole.fromValue(membershipDetails.getProjectRole().getRoleName().toUpperCase()));

}
