package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.store.jooq.enums.JProjectRoleEnum;
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

	private Map<String, JProjectRoleEnum> projectRoles;

	public ReportPortalUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
			Map<String, JProjectRoleEnum> projectRoles) {
		super(username, password, authorities);
		this.projectRoles = projectRoles;
	}

	public ReportPortalUser(User user, Map<String, JProjectRoleEnum> projectRoles) {
		this(user.getUsername(), user.getPassword(), user.getAuthorities(), projectRoles);
	}

	public Map<String, JProjectRoleEnum> getProjectRoles() {
		return projectRoles;
	}
}
