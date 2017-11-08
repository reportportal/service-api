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

import com.epam.ta.reportportal.auth.AuthConstants;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MVC Pageable test
 *
 * @author Andrei Varabyeu
 */
public class MvcPageableTest extends BaseMvcTest {

	private static final String URL_PATTERN = "/launch?page.sort=start_time&page.sort.dir=ASC&page.page=%s&page.size=1";

	@Test
	public void testPageableZero() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + getPageUrl("1")).principal(AuthConstants.ADMINISTRATOR)
				.secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.page.number").value(1));

	}

	@Test
	public void testPageableLast() throws Exception {
		ResultActions resultActions = this.mvcMock.perform(get(PROJECT_BASE_URL + getPageUrl("3")).principal(AuthConstants.ADMINISTRATOR)
				.secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))).andExpect(status().isOk());

		resultActions.andExpect(content().contentType("application/json;charset=UTF-8")).andExpect(jsonPath("$.page.number").value(3));
	}

	private String getPageUrl(String pageNumber) {
		return String.format(URL_PATTERN, pageNumber);
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}