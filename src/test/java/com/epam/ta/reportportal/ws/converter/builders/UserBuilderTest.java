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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class UserBuilderTest extends BaseTest {

	@Autowired
	private Provider<UserBuilder> userBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testBeanScope() {
		Assert.assertTrue(
				"User builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(UserBuilder.class)[0])
		);
	}

	@Test
	public void testNull() {
		User actualValue = userBuilderProvider.get().addCreateUserRQ(null).addUserRole(null).build();
		User expectedValue = new User();
		validateUsers(expectedValue, actualValue);
	}

	@Test
	public void testValues() {
		User actualValue = userBuilderProvider.get().addCreateUserRQ(getCreateUserRQ()).addUserRole(UserRole.USER).build();
		validateUsers(Utils.getUser(), actualValue);

	}

	private CreateUserRQConfirm getCreateUserRQ() {
		CreateUserRQConfirm request = new CreateUserRQConfirm();
		request.setEmail(BuilderTestsConstants.EMAIL);
		request.setDefaultProject(BuilderTestsConstants.PROJECT);
		request.setLogin(BuilderTestsConstants.NAME);
		request.setPassword(BuilderTestsConstants.PASSWORD);
		return request;
	}

	private void validateUsers(User expectedValue, User actualValue) {
		Assert.assertEquals(expectedValue.getEmail(), actualValue.getEmail());
		if (null != expectedValue.getLogin()) {
			Assert.assertEquals(expectedValue.getLogin().toLowerCase(), actualValue.getLogin());
		}

		Assert.assertEquals(expectedValue.getPassword(), actualValue.getPassword());
		Assert.assertEquals(expectedValue.getRole(), actualValue.getRole());
	}
}