/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import javax.validation.constraints.NotNull;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;

/**
 * Validates this is {@link ProjectRole#LEAD} or higher authority in the
 * authentication context
 * 
 * @author Andrei Varabyeu
 * 
 */
@Component
@LookupPermission({ "projectLeadPermission" })
public class ProjectLeadPermission extends BaseProjectPermission {

	/**
	 * Validates this is {@link ProjectRole#LEAD} or higher authority in the
	 * authentication context
	 */
	@Override
	protected boolean checkAllowed(@NotNull Authentication authentication, @NotNull Project project) {
		return project.getUsers().get(authentication.getName()).getProjectRole().compareTo(ProjectRole.LEAD) >= 0;
	}
}