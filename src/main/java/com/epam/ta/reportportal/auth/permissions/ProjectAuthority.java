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
