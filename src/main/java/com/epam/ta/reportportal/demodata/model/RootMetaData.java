package com.epam.ta.reportportal.demodata.model;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;

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
