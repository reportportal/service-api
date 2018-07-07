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

	private Map<String, ProjectDetails> projectDetails;

	public ReportPortalUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long userId, UserRole role,
			Map<String, ProjectDetails> projectDetails) {
		super(username, password, authorities);
		this.userId = userId;
		this.userRole = role;
		this.projectDetails = projectDetails;
	}

	public ReportPortalUser(User user, Long userId, UserRole role, Map<String, ProjectDetails> projectDetails) {
		this(user.getUsername(), user.getPassword(), user.getAuthorities(), userId, role, projectDetails);
	}

	public Long getUserId() {
		return userId;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public Map<String, ProjectDetails> getProjectDetails() {
		return projectDetails;
	}

	public static class ProjectDetails {

		private Long projectId;
		private ProjectRole projectRole;

		public ProjectDetails(Long projectId, ProjectRole projectRole) {
			this.projectId = projectId;
			this.projectRole = projectRole;
		}

		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		public ProjectRole getProjectRole() {
			return projectRole;
		}

		public void setProjectRole(ProjectRole projectRole) {
			this.projectRole = projectRole;
		}
	}
}
