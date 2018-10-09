package com.epam.ta.reportportal.core.user.event;

import com.epam.ta.reportportal.entity.user.UserRole;

/**
 * @author Ivan Budaev
 */
public class UpdatedRole {

	private String username;
	private UserRole role;

	public UpdatedRole(String username, UserRole role) {
		this.username = username;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}
}
