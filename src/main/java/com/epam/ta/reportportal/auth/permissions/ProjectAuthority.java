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
package com.epam.ta.reportportal.auth.permissions;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

/**
 * Project authority
 *
 * @author Andrei Varabyeu
 */
public class ProjectAuthority implements GrantedAuthority {

	private final String project;
	private final String projectRole;

	public ProjectAuthority(String project, String projectRole) {
		this.project = project;
		this.projectRole = projectRole;
	}

	public String getProject() {
		return project;
	}

	public String getProjectRole() {
		return projectRole;
	}

	@Override
	public String getAuthority() {
		return "PROJECT_" + project + "_" + projectRole;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProjectAuthority that = (ProjectAuthority) o;
		return Objects.equals(project, that.project) && Objects.equals(projectRole, that.projectRole);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project, projectRole);
	}
}
