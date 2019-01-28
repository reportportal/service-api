/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Map;

/**
 * ReportPortal user representation
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class ReportPortalUser extends User {

	private Long userId;

	private UserRole userRole;

	private String email;

	private Map<String, ProjectDetails> projectDetails;

	public ReportPortalUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long userId,
			UserRole role, Map<String, ProjectDetails> projectDetails, String email) {
		super(username, password, authorities);
		this.userId = userId;
		this.userRole = role;
		this.projectDetails = projectDetails;
		this.email = email;
	}

	public ReportPortalUser(User user, Long userId, UserRole role, Map<String, ProjectDetails> projectDetails, String email) {
		this(user.getUsername(), user.getPassword(), user.getAuthorities(), userId, role, projectDetails, email);
	}

	public Long getUserId() {
		return userId;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public String getEmail() {
		return email;
	}

	public Map<String, ProjectDetails> getProjectDetails() {
		return projectDetails;
	}

	public static class ProjectDetails {

		private Long projectId;

		private String projectName;

		private ProjectRole projectRole;

		public ProjectDetails(Long projectId, String projectName, ProjectRole projectRole) {
			this.projectId = projectId;
			this.projectName = projectName;
			this.projectRole = projectRole;
		}

		public Long getProjectId() {
			return projectId;
		}

		public String getProjectName() {
			return projectName;
		}

		public ProjectRole getProjectRole() {
			return projectRole;
		}
	}
}
