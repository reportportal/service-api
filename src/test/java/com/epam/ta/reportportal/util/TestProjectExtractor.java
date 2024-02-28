package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import java.util.Optional;

public class TestProjectExtractor {

  public static ReportPortalUser.ProjectDetails extractProjectDetails(ReportPortalUser user,
      String projectKey) {
    final String normalizedProjectName = normalizeId(projectKey);
    return Optional.ofNullable(user.getProjectDetails().get(normalizedProjectName))
        .orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
            "Please check the list of your available projects."
        ));
  }

}
