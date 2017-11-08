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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for sorting parameters
 *
 * @author Andrei Varabyeu
 */
public class SortingMvcTest extends BaseMvcTest {

	private static final String URL_PATTERN_NAME_SORTING = "/launch?page.sort=name,%s";

	private static final String URL_PATTERN_END_TIME_SORTING = "/launch?page.sort=end_time,%s";

	private static final String URL_PATTERN_NAME_DESC_SORTING = "/launch?page.sort=name,description,%s";

	@Test
	public void testDesc() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + getPageUrl(Sort.Direction.DESC)).principal(AuthConstants.ADMINISTRATOR)
				.secure(true)
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.content[0].name").value("Demo launch_launch1Last"));

	}

	@Test
	public void testAsc() throws Exception {
		ResultActions resultActions = this.mvcMock.perform(
				get(PROJECT_BASE_URL + getPageUrl(Sort.Direction.ASC)).principal(AuthConstants.ADMINISTRATOR)
						.secure(true)
						.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))).andExpect(status().isOk());

		ResultActions resultActions1 = resultActions.andExpect(content().contentType("application/json;charset=UTF-8"));
		MvcResult mvcResult = resultActions1.andReturn();
		System.out.println(mvcResult.getResponse().getHeader("Content-Type"));
		System.out.println(mvcResult.getResponse().getContentAsString());
		resultActions1.andExpect(jsonPath("$.content[0].name").value("Demo launch"));
	}

	@Test
	public void testSeveralParameters() throws Exception {
		ResultActions resultActions = this.mvcMock.perform(
				get(PROJECT_BASE_URL + String.format(URL_PATTERN_NAME_DESC_SORTING, Direction.DESC)).principal(AuthConstants.ADMINISTRATOR)
						.secure(true)
						.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))).andExpect(status().isOk());

		resultActions.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.content[1].description").value("AAA-FIRST - Another Description"));
	}

	@Ignore
	@Test
	public void testEndTime() throws Exception {
		ResultActions resultActions = this.mvcMock.perform(
				get(PROJECT_BASE_URL + String.format(URL_PATTERN_END_TIME_SORTING, Sort.Direction.DESC)).principal(
						AuthConstants.ADMINISTRATOR).secure(true).accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk());

		resultActions.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.content[0].end_time").value(1367493780000L));
	}

	private String getPageUrl(Sort.Direction direction) {
		return String.format(URL_PATTERN_NAME_SORTING, direction.name());
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}

}