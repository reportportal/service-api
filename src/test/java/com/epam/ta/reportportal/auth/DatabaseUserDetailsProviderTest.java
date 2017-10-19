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

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@SpringFixture("authTests")
@Ignore
public class DatabaseUserDetailsProviderTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	@Qualifier("daoUserDetailsService")
	private UserDetailsService userDetailsService;

	@Test
	public void testDetailLoading() {
		UserDetails details = userDetailsService.loadUserByUsername(AuthConstants.TEST_USER);

		Assert.assertEquals("Incorrect userName", AuthConstants.TEST_USER, details.getUsername());
		Assert.assertEquals("Incorrect password", DigestUtils.md5Hex(AuthConstants.USER_PASSWORD), details.getPassword());

		Assert.assertEquals("Incorrect Granted Authorities size", 1, details.getAuthorities().size());

		GrantedAuthority authority = details.getAuthorities().iterator().next();
		Assert.assertEquals("Incorrect Granted Authorities", AuthConstants.ROLE.name(), authority.getAuthority());

	}
}