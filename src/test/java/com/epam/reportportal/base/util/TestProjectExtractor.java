package com.epam.reportportal.base.util;

import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;

public class TestProjectExtractor {

  public static MembershipDetails extractProjectDetails(ReportPortalUser user, String projectKey) {
    return Optional.ofNullable(rpUserToMembership(user))
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
            "Please check the list of your available projects."
        ));
  }

}
