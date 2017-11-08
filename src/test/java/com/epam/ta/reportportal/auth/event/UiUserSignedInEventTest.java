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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

//import com.epam.ta.reportportal.commons.auth.event.UiUserSignedInEvent;

/**
 * Checks UiUserSignedIn event
 *
 * @author Andrei Varabyeu
 */
@Ignore
public class UiUserSignedInEventTest extends BaseMvcTest {

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void testEventPublishing() {
		// eventPublisher.publishEvent(
		// new UiUserSignedInEvent(new
		// UsernamePasswordAuthenticationToken(AuthConstants.TEST_USER,
		// AuthConstants.USER_PASSWORD)));
		User userAfterLogin = userRepository.findOne(AuthConstants.TEST_USER);
		Assert.assertThat(userAfterLogin.getMetaInfo().getLastLogin(), not(nullValue()));

	}

	@Override
	protected Authentication authentication() {
		return new UsernamePasswordAuthenticationToken(AuthConstants.TEST_USER, AuthConstants.USER_PASSWORD);
	}
}