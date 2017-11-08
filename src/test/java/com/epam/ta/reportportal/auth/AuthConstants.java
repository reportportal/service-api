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

package com.epam.ta.reportportal.auth;

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.google.common.collect.ImmutableList;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Set of test constants for AUTH tests. <br>
 * Data from {@link /ws/src/test/resources/db/setupData.xml} fixtures
 *
 * @author Andrei Varabyeu
 */
public abstract class AuthConstants {

	/**
	 * Demo user name
	 */
	public static final String TEST_USER = "user1";

	public static final String TEST_UPSA_USER = "upsa_user";

	/**
	 * Demo password
	 */
	public static final String USER_PASSWORD = "1q2w3e";

	/**
	 * Project of demo user
	 */
	public static final String USER_PROJECT = "project1";

	/**
	 * Demo {@link UserRole}
	 */
	public static final UserRole ROLE = UserRole.ADMINISTRATOR;

	public static final Authentication ADMINISTRATOR = newAuthentication(AuthConstants.TEST_USER, AuthConstants.USER_PASSWORD, true,
			new SimpleGrantedAuthority(UserRole.ADMINISTRATOR.getAuthority())
	);

	public static final Authentication PROJECT_USER = newAuthentication(AuthConstants.TEST_USER, AuthConstants.USER_PASSWORD, true,
			new SimpleGrantedAuthority(UserRole.ADMINISTRATOR.getAuthority())
	);

	public static final Authentication NOT_AUTHENTIFICATED = newAuthentication(AuthConstants.TEST_USER, AuthConstants.USER_PASSWORD, false);

	public static final Authentication UPSA_USER = newAuthentication(AuthConstants.TEST_UPSA_USER, AuthConstants.USER_PASSWORD, true,
			new SimpleGrantedAuthority(UserRole.ADMINISTRATOR.getAuthority())
	);

	/**
	 * Constructrs authentification using provided credentials and authorities
	 *
	 * @param user
	 * @param password
	 * @param authenticated
	 * @param authorities
	 * @return
	 */
	public static Authentication newAuthentication(String user, String password, final boolean authenticated,
			GrantedAuthority... authorities) {
		return new TestingAuthenticationToken(
				user, password, ImmutableList.<org.springframework.security.core.GrantedAuthority>builder().add(authorities).build()) {
			private static final long serialVersionUID = 1L;

			{
				setAuthenticated(authenticated);
			}
		};
	}
}