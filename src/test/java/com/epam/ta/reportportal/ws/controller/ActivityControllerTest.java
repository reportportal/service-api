/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/activity/activity-fill.sql")
public class ActivityControllerTest extends BaseMvcTest {

	@Before
	public void setUp() throws Exception {
		System.err.println(Thread.currentThread().getId());
	}

	@Test
	public void getActivityByWrongTestItemId() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/activity/1111").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(404));
	}

	@Test
	public void getActivityByWrongProjectName() throws Exception {
		mockMvc.perform(get("/wrong_project/activity/1").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(403));
	}

	@Test
	public void getTestItemActivitiesByWrongTestItem() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/activity/item/1111").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(404));
	}

	@Test
	public void getTestItemActivitiesPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/activity/item/1").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	public void getActivityPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/activity/1").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	public void getActivitiesForProject() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/activity").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}
}