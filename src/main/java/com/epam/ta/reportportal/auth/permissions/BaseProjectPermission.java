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
import com.epam.ta.reportportal.store.database.entity.project.ProjectRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Objects;

/**
 * Base logic for project-related permissions. Validates project exists and
 * there is provided in {@link Authentication} user assigned to this project
 *
 * @author Andrei Varabyeu
 */
abstract class BaseProjectPermission implements Permission {

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

		ProjectRole role = rpUser.getProjectRoles().get(projectName.toString());
		return checkAllowed(rpUser, projectName.toString(), role);
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