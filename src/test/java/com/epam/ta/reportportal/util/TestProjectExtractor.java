package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

public class TestProjectExtractor {

	public static ReportPortalUser.ProjectDetails extractProjectDetails(ReportPortalUser user, String projectName) {
		final String normalizedProjectName = normalizeId(projectName);
			return Optional.ofNullable(user.getProjectDetails().get(normalizedProjectName))
					.orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED,
							"Please check the list of your available projects."
					));
	}

}
