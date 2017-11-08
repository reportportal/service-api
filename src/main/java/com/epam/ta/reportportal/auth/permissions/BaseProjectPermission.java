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

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import javax.inject.Provider;
import javax.validation.constraints.NotNull;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;

/**
 * Base logic for project-related permissions. Validates project exists and
 * there is provided in {@link Authentication} user assigned to this project
 *
 * @author Andrei Varabyeu
 */
abstract class BaseProjectPermission implements Permission {

	/*
	 * Due to Spring's framework flow, Security API loads first. So, context
	 * doesn't know anything about Repository beans. We have to load this beans
	 * lazily
	 */
	@Autowired
	private Provider<ProjectRepository> projectRepository;

	/**
	 * Validates project exists and user assigned to project. After that
	 * delegates permission check to subclass
	 */
	@Override
	public boolean isAllowed(Authentication authentication, Object projectName) {
		if (!authentication.isAuthenticated()) {
			return false;
		}

		String project = (String) projectName;
		Project p = projectRepository.get().findOne(project);
		BusinessRule.expect(p, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, project);

		BusinessRule.expect(ProjectUtils.doesHaveUser(p, authentication.getName()), equalTo(true)).verify(ErrorType.ACCESS_DENIED);
		return checkAllowed(authentication, p);
	}

	/**
	 * Validates permission
	 *
	 * @param authentication Authentication object
	 * @param project        ReportPortal's Project
	 * @return TRUE if access allowed
	 */
	abstract protected boolean checkAllowed(@NotNull Authentication authentication, @NotNull Project project);
}