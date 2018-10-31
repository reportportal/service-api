/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

/**
 * @author Pavel Bortnik
 */
public class ProjectExtractor {

	/**
	 * Extracts project details for specified user by specified project name
	 *
	 * @param user        User
	 * @param projectName Project name
	 * @return Project Details
	 */
	public static ReportPortalUser.ProjectDetails extractProjectDetails(ReportPortalUser user, String projectName) {
		return Optional.ofNullable(user.getProjectDetails().get(normalizeId(projectName)))
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
	}

}
