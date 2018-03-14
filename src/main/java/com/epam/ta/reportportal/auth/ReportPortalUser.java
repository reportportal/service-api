package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.store.database.entity.project.ProjectRole;
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

	private Map<String, ProjectRole> projectRoles;

	public ReportPortalUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
			Map<String, ProjectRole> projectRoles) {
		super(username, password, authorities);
		this.projectRoles = projectRoles;
	}

	public ReportPortalUser(User user, Map<String, ProjectRole> projectRoles) {
		this(user.getUsername(), user.getPassword(), user.getAuthorities(), projectRoles);
	}

	public Map<String, ProjectRole> getProjectRoles() {
		return projectRoles;
	}
}
