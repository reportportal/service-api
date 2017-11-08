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

package com.epam.ta.reportportal.ws;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringFixture("mvcTests")
public abstract class BaseMvcTest extends BaseTest {

	protected static final String PROJECT_BASE_URL = "/" + AuthConstants.USER_PROJECT;

	@Autowired
	protected WebApplicationContext wac;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	protected MockMvc mvcMock;

	@Before
	public void setup() {
		this.mvcMock = MockMvcBuilders.webAppContextSetup(this.wac).build();
		SecurityContextHolder.getContext().setAuthentication(authentication());
	}

	@After
	public void teardown() {
		SecurityContextHolder.clearContext();
	}

	abstract protected Authentication authentication();
}