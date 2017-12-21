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

package com.epam.ta.reportportal.ws.resolver;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC test for validation Jackson2 view serialization
 *
 * @author Andrei Varabyeu
 */
public class MvcJacksonViewsTest extends BaseMvcTest {

	/**
	 * Validate that there is no view-related fields included in response marked
	 * with default view
	 *
	 * @throws Exception
	 */
	@Test
	public void testDefaultView() throws Exception {
		this.mvcMock.perform(get("/project/" + PROJECT_BASE_URL + "/users").secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"));

	}

	/**
	 * Validate that view-specific fields are included into response marked with
	 * specific view
	 *
	 * @throws Exception
	 */
	@Test
	public void testSpecificView() throws Exception {
		this.mvcMock.perform(get("/user/" + AuthConstants.TEST_USER).principal(AuthConstants.ADMINISTRATOR)
				.secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.userId").exists());

	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}