package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import java.util.Optional;

public class TestProjectExtractor {

  public static MembershipDetails extractProjectDetails(ReportPortalUser user, String projectKey) {
    return Optional.ofNullable(rpUserToMembership(user))
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
            "Please check the list of your available projects."
        ));
  }

}
