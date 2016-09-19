/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.events.handler.LaunchActivityHandler.START;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.epam.ta.reportportal.database.entity.Launch;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.epam.ta.reportportal.auth.AuthConstants;
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * Base MVC test for Launch Controller
 *
 * @author Andrei Varabyeu
 */
public class LaunchControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ActivityRepository activityRepository;

	@Test
	public void happyCreateLaunch() throws Exception {
		String name = "some launch name";
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName(name);
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setMode(DEFAULT);

		ResultActions resultActions = mvcMock.perform(post(PROJECT_BASE_URL + "/launch/").principal(authentication())
				.content(objectMapper.writeValueAsBytes(startLaunchRQ)).contentType(APPLICATION_JSON));
		resultActions.andExpect(status().isCreated());
		MvcResult mvcResult = resultActions.andReturn();
		EntryCreatedRS entryCreatedRS = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), EntryCreatedRS.class);
		List<Activity> activities = activityRepository.findByLoggedObjectRef(entryCreatedRS.getId());
		assertNotNull(activities);
		assertEquals(1, activities.size());
		Activity activity = activities.get(0);
		assertEquals(START, activity.getActionType());
		assertEquals(Launch.LAUNCH, activity.getObjectType());
	}

	@Test
	public void updateLaunchPositive() throws Exception {
		UpdateLaunchRQ rq = new UpdateLaunchRQ();
		rq.setMode(DEFAULT);
		rq.setTags(new HashSet<String>() {
			private static final long serialVersionUID = 1L;

			{
				add("tag");
			}
		});
		rq.setDescription("description");
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/launch/51824cc1553de743b3e5aa2c/update").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq)).contentType(APPLICATION_JSON)).andExpect(status().is(200));
	}

	@Test
	public void deleteLaunchPositive() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/launch/88624678053de743b3e5aa3e").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLaunchPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/launch/51824cc1323de743b3e5aa2c").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getDebugLaunches() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/launch/mode").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void compareLaunches() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/launch/compare?ids=51824cc1553de743b3e5aa2c").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void mergeLaunchesPositive() throws Exception {
		MergeLaunchesRQ rq = new MergeLaunchesRQ();
		HashSet<String> set = new HashSet<>();
		set.add("88624678053de743b3e5aa3e");
		rq.setLaunches(set);
		rq.setName("Merged");
		rq.setStartTime(new Date());
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/launch/merge").contentType(APPLICATION_JSON).principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void startLaunchAnalyzerPositive() throws Exception {
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/launch/88624678053de743b3e5aa3e/analyze/history").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getStatus() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/launch/status?ids=88624678053de743b3e5aa3e").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void finishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setStatus(Status.PASSED.name());
		this.mvcMock
				.perform(put(PROJECT_BASE_URL + "/launch/51824cc1553de743b4e5aa2c/finish").contentType(APPLICATION_JSON)
						.principal(authentication()).content(objectMapper.writeValueAsBytes(finishExecutionRQ)))
				.andExpect(status().is(200));
	}

	@Test
	public void forceFinishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setStatus(Status.PASSED.name());
		this.mvcMock
				.perform(put(PROJECT_BASE_URL + "/launch/5187cba4553d2fdd93969fcd/stop").contentType(APPLICATION_JSON)
						.principal(authentication()).content(objectMapper.writeValueAsBytes(finishExecutionRQ)))
				.andExpect(status().is(200));
	}

	@Test
	public void getTags() throws Exception {
		this.mvcMock.perform(
				get(PROJECT_BASE_URL + "/launch/tags?filter.cnt.tags=tag").contentType(APPLICATION_JSON).principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getAllOwners() throws Exception {
		this.mvcMock.perform(
				get(PROJECT_BASE_URL + "/launch/owners?filter.cnt.user=user").contentType(APPLICATION_JSON).principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getAllLaunchNames() throws Exception {
		this.mvcMock.perform(
				get(PROJECT_BASE_URL + "/launch/names?filter.cnt.name=name").contentType(APPLICATION_JSON).principal(authentication()))
				.andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}