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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Test;
import org.springframework.security.core.Authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base MVC test for ITestItemController
 *
 * @author Dzmitry_Kavalets
 */
public class ActivityControllerTest extends BaseMvcTest {

	@Test
	public void getActivitiesByWrongTestItemId() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/activity/dfjhjkshgfjksdgfd").principal(authentication())).andExpect(status().is(404));
	}

	@Test
	public void getActivitiesByWrongProjectName() throws Exception {
		this.mvcMock.perform(get("/wrong_project/activity").principal(authentication())).andExpect(status().is(404));
	}

	@Test
	public void getTestItemActivitiesByWrongTestItem() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/activity/item/asdadsa").principal(authentication())).andExpect(status().is(404));
	}

	@Test
	public void getTestItemActivitiesPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "activity/item/44524cc1553de753b3e5bb2f").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getActivityPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/activity/532aadfea331a2d4cf4aa08e").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getTestItemActivities() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/activity/item/44524cc1553de753b3e5bb2f").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}