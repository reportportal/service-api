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

package com.epam.reportportal.demodata.model;

import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;

public class RootMetaData {

  private final String launchUuid;
  private final ReportPortalUser user;
  private final MembershipDetails membershipDetails;

  private RootMetaData(String launchUuid, ReportPortalUser user,
      MembershipDetails membershipDetails) {
    this.launchUuid = launchUuid;
    this.user = user;
    this.membershipDetails = membershipDetails;
  }

  public String getLaunchUuid() {
    return launchUuid;
  }

  public ReportPortalUser getUser() {
    return user;
  }

  public MembershipDetails getProjectDetails() {
    return membershipDetails;
  }

  public static RootMetaData of(String launchUuid, ReportPortalUser user,
      MembershipDetails membershipDetails) {
    return new RootMetaData(launchUuid, user, membershipDetails);
  }
}
