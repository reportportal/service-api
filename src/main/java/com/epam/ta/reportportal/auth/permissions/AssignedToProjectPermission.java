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
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Check whether user assigned to project
 *
 * @author Andrei Varabyeu
 */
@Component("assignedToProjectPermission")
@LookupPermission({ "isAssignedToProject" })
class AssignedToProjectPermission implements Permission {

	/*
	 * Due to Spring's framework flow, Security API loads first. So, context
	 * doesn't know anything about Repository beans. We have to load this beans
	 * lazily
	 */
	private final ProjectExtractor projectExtractor;

	@Autowired
	AssignedToProjectPermission(ProjectExtractor projectExtractor) {
		this.projectExtractor = projectExtractor;
	}

	/**
	 * Check whether user assigned to project<br>
	 * Or user is ADMIN who is GOD of ReportPortal
	 */
	@Override
	public boolean isAllowed(Authentication authentication, Object targetDomainObject) {
		if (!authentication.isAuthenticated()) {
			return false;
		}

		OAuth2Authentication oauth = (OAuth2Authentication) authentication;
		ReportPortalUser rpUser = (ReportPortalUser) oauth.getUserAuthentication().getPrincipal();
		BusinessRule.expect(rpUser, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

		final String resolvedProjectName = String.valueOf(targetDomainObject);
		final Optional<ReportPortalUser.ProjectDetails> projectDetails = projectExtractor.findProjectDetails(rpUser, resolvedProjectName);
		projectDetails.ifPresent(details -> fillProjectDetails(rpUser, resolvedProjectName, details));
		return projectDetails.isPresent();
	}

	private void fillProjectDetails(ReportPortalUser rpUser, String resolvedProjectName, ReportPortalUser.ProjectDetails projectDetails) {
		final Map<String, ReportPortalUser.ProjectDetails> projectDetailsMapping = Maps.newHashMapWithExpectedSize(1);
		projectDetailsMapping.put(resolvedProjectName, projectDetails);
		rpUser.setProjectDetails(projectDetailsMapping);
	}

	private boolean hasProjectAuthority(Collection<? extends GrantedAuthority> authorityList, String project) {
		return authorityList.stream()
				.filter(a -> a instanceof ProjectAuthority)
				.anyMatch(pa -> ((ProjectAuthority) pa).getProject().equals(project));
	}
}