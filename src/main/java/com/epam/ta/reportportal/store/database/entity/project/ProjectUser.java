/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.store.database.entity.project;

import com.epam.ta.reportportal.store.database.entity.enums.ProjectRoleEnum;
import com.epam.ta.reportportal.store.database.entity.user.Users;

/**
 * @author Pavel Bortnik
 */
public class ProjectUser {

	private Project project;

	private Users user;

	private ProjectRoleEnum projectRole;

	public ProjectUser() {
	}

	public ProjectUser(Project project, Users user, ProjectRoleEnum projectRole) {
		this.project = project;
		this.user = user;
		this.projectRole = projectRole;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public ProjectRoleEnum getProjectRole() {
		return projectRole;
	}

	public void setProjectRole(ProjectRoleEnum projectRole) {
		this.projectRole = projectRole;
	}
}
