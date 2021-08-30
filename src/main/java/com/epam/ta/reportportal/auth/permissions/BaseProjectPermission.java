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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Maps;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Map;
import java.util.Objects;

/**
 * Base logic for project-related permissions. Validates project exists and
 * there is provided in {@link Authentication} user assigned to this project
 *
 * @author Andrei Varabyeu
 */
abstract class BaseProjectPermission implements Permission {

	private final ProjectExtractor projectExtractor;

	protected BaseProjectPermission(ProjectExtractor projectExtractor) {
		this.projectExtractor = projectExtractor;
	}

	/**
	 * Validates project exists and user assigned to project. After that
	 * delegates permission check to subclass
	 */
	@Override
	public boolean isAllowed(Authentication authentication, Object projectName) {
		if (!authentication.isAuthenticated()) {
			return false;
		}

		OAuth2Authentication oauth = (OAuth2Authentication) authentication;
		ReportPortalUser rpUser = (ReportPortalUser) oauth.getUserAuthentication().getPrincipal();
		BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

		final String resolvedProjectName = String.valueOf(projectName);
		final ReportPortalUser.ProjectDetails projectDetails = projectExtractor.findProjectDetails(rpUser, resolvedProjectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));
		fillProjectDetails(rpUser, resolvedProjectName, projectDetails);

		ProjectRole role = projectDetails.getProjectRole();
		return checkAllowed(rpUser, projectName.toString(), role);
	}

	private void fillProjectDetails(ReportPortalUser rpUser, String resolvedProjectName, ReportPortalUser.ProjectDetails projectDetails) {
		final Map<String, ReportPortalUser.ProjectDetails> projectDetailsMapping = Maps.newHashMapWithExpectedSize(1);
		projectDetailsMapping.put(resolvedProjectName, projectDetails);
		rpUser.setProjectDetails(projectDetailsMapping);
	}

	/**
	 * Validates permission
	 *
	 * @param user    ReportPortal user object
	 * @param project ReportPortal's Project name
	 * @param role    User role
	 * @return TRUE if access allowed
	 */
	abstract protected boolean checkAllowed(ReportPortalUser user, String project, ProjectRole role);
}