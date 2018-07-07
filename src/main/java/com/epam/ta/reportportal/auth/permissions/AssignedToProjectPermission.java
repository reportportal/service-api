/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.auth.permissions;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.Collection;
import java.util.Objects;

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
	@Autowired
	private Provider<ProjectRepository> projectRepository;

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
		ProjectRole role = rpUser.getProjectDetails().get(targetDomainObject.toString()).getProjectRole();

		return role != null;
	}

	private boolean hasProjectAuthority(Collection<? extends GrantedAuthority> authorityList, String project) {
		return authorityList.stream()
				.filter(a -> a instanceof ProjectAuthority)
				.anyMatch(pa -> ((ProjectAuthority) pa).getProject().equals(project));
	}
}