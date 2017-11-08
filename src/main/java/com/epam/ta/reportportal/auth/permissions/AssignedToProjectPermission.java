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

import com.epam.ta.reportportal.database.dao.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.Collection;

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
	public boolean isAllowed(Authentication authentication, Object projectName) {
		String project = (String) projectName;
		return authentication.isAuthenticated() && (hasProjectAuthority(authentication.getAuthorities(), project) || projectRepository.get()
				.isAssignedToProject(project, authentication.getName()));
	}

	private boolean hasProjectAuthority(Collection<? extends GrantedAuthority> authorityList, String project) {
		return authorityList.stream()
				.filter(a -> a instanceof ProjectAuthority)
				.anyMatch(pa -> ((ProjectAuthority) pa).getProject().equals(project));
	}
}