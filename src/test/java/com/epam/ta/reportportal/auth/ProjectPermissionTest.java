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
import com.epam.ta.reportportal.auth.permissions.Permission;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * ProjectPermissionTest
 *
 * @author Andrei Varabyeu
 */
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@SpringFixture("authTests")
public class ProjectPermissionTest extends BaseTest {

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	@Qualifier("assignedToProjectPermission")
	private Permission projectPermission;

	@Test
	public void testProjectPermission() {
		Assert.assertTrue("Correct permission is not allowed",
				projectPermission.isAllowed(AuthConstants.ADMINISTRATOR, AuthConstants.USER_PROJECT)
		);
	}

	@Test
	public void testWrongProjectPermission() {
		Assert.assertFalse("Allowed with wrong password", projectPermission.isAllowed(AuthConstants.ADMINISTRATOR, "wrong project"));
	}

	@Test
	public void testNotAuthenticatedPermission() {
		Assert.assertFalse("Correct permission is not allowed",
				projectPermission.isAllowed(AuthConstants.NOT_AUTHENTIFICATED, AuthConstants.USER_PROJECT)
		);
	}
}