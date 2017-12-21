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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

/**
 * Created by Andrey_Ivanov1 on 05-Jun-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class UserRoleHierarchyTest {

	@InjectMocks
	private UserRoleHierarchy userRoleHierarchy = new UserRoleHierarchy();

	@Test
	public void getReachableGrantedAuthoritiesTest() {
		String string_for_auth = "ROLE_1,ROLE_2,ROLE_3,ROLE_4";
		Collection<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(string_for_auth);
		Assert.assertNotNull(userRoleHierarchy.getReachableGrantedAuthorities(authorities));
	}

	@Test
	public void nullAuthoritiesTest() {
		Collection<GrantedAuthority> authorities = null;
		Assert.assertNotNull(userRoleHierarchy.getReachableGrantedAuthorities(authorities));
	}

}