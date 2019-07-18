/*
 * Copyright 2019 EPAM Systems
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