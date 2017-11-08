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

package com.epam.ta.reportportal.auth.event;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test last login auth event
 *
 * @author Andrei Varabyeu
 */
@Ignore
public class AuthenticationSuccessEventTest extends BaseMvcTest {

	@Autowired
	private UserRepository userRepository;

	/**
	 * Validate that login via API key doesn't forces update of last_login entry
	 * in database
	 */
	@Test
	public void testEvent() throws Exception {
		User userBeforeLogin = userRepository.findOne(AuthConstants.TEST_USER);
		Assert.assertThat(userBeforeLogin.getMetaInfo().getLastLogin(), nullValue());

		this.mvcMock.perform(get(PROJECT_BASE_URL + "/launch/51824cc1553de743b3e5aa2c").secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
				.principal(authentication())).andExpect(status().is(200));

		User userAfterLogin = userRepository.findOne(AuthConstants.TEST_USER);

		Assert.assertNotEquals(userAfterLogin.getMetaInfo().getLastLogin(), userBeforeLogin.getMetaInfo().getLastLogin());
	}

	@Override
	protected Authentication authentication() {
		return new UsernamePasswordAuthenticationToken(AuthConstants.TEST_USER, AuthConstants.USER_PASSWORD);
	}
}