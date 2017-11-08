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

package com.epam.ta.reportportal.core.user;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;

/**
 * Delete user related tests
 *
 * @author Dzmitry_Kavalets
 */
@SpringFixture("deleteUserHandlerTest")
public class DeleteUserTest extends BaseTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private IDeleteUserHandler deleteUserHandler;

	@Autowired
	private LaunchRepository launchRepository;

	@Test
	public void positiveDeleteUserTest() {
		String delUserId = "user1";
		String adminUserId = "user2";
		deleteUserHandler.deleteUser(delUserId, adminUserId);
		Launch debugMode = launchRepository.findOne("51824cc1323de743b3e5aa2c");
		Launch defaultMode = launchRepository.findOne("51824cc1323de743b3e5aa5c");
		Assert.assertNotNull(debugMode);
		Assert.assertNotNull(defaultMode);
		Assert.assertNotNull(defaultMode.getUserRef());
		Assert.assertEquals("user1", defaultMode.getUserRef());
	}

	@Test
	public void negativeDeleteUserTest() {
		String delUserId = "user1";
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(formattedSupplier(INCORRECT_REQUEST.getDescription(), "You cannot delete own account").get());
		deleteUserHandler.deleteUser(delUserId, delUserId);
	}
}