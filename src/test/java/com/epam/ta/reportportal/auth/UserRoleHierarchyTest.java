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

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by Andrey_Ivanov1 on 05-Jun-17.
 */
class UserRoleHierarchyTest {

	@InjectMocks
	private UserRoleHierarchy userRoleHierarchy = new UserRoleHierarchy();

	@Test
	void getReachableGrantedAuthoritiesTest() {
		String string_for_auth = "ROLE_1,ROLE_2,ROLE_3,ROLE_4";
		Collection<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(string_for_auth);
		assertNotNull(userRoleHierarchy.getReachableGrantedAuthorities(authorities));
	}

	@Test
	void nullAuthoritiesTest() {
		Collection<GrantedAuthority> authorities = null;
		assertNotNull(userRoleHierarchy.getReachableGrantedAuthorities(authorities));
	}
}