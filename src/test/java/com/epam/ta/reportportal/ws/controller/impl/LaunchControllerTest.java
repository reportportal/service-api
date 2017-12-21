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

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.AuthConstants.ADMINISTRATOR;
import static com.epam.ta.reportportal.auth.AuthConstants.USER_PROJECT;
import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.START_LAUNCH;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.LAUNCH;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	@Autowired
	private LaunchRepository launchRepository;
	@Autowired
	private LaunchController launchController;

	@Test
	public void happyCreateLaunch() throws Exception {
		String name = "some launch name";
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName(name);
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setMode(DEFAULT);

		ResultActions resultActions = mvcMock.perform(post(PROJECT_BASE_URL + "/launch/").principal(authentication())
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON));
		resultActions.andExpect(status().isCreated());
		MvcResult mvcResult = resultActions.andReturn();
		EntryCreatedRS entryCreatedRS = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), EntryCreatedRS.class);
		List<Activity> activities = activityRepository.findByLoggedObjectRef(entryCreatedRS.getId());
		assertNotNull(activities);
		assertEquals(1, activities.size());
		Activity activity = activities.get(0);
		assertEquals(START_LAUNCH, activity.getActionType());
		assertEquals(LAUNCH, activity.getObjectType());
	}

	@Test
	public void importLaunch() throws Exception {
		Path file = Paths.get("src/test/resources/test-results.zip");
		MockMultipartFile multipartFile = new MockMultipartFile("test-results.zip", "test-results.zip", "application/zip",
				Files.readAllBytes(file)
		);
		OperationCompletionRS response = launchController.importLaunch("project1", multipartFile, authentication());
		String id = response.getResultMessage()
				.substring(response.getResultMessage().indexOf("=") + 1, response.getResultMessage().indexOf("is"))
				.trim();
		Launch launch = launchRepository.findOne(id);
		assertNotNull(launch);
		assertTrue(launchRepository.hasItems(launch, Status.FAILED));
		assertEquals(launch.getName(), "test-results");
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
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
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
		set.add("89224678053de743b3e5aa3e");
		rq.setLaunches(set);
		rq.setName("Merged");
		rq.setMergeStrategyType("BASIC");
		rq.setStartTime(new Date());
		rq.setEndTime(new Date());
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/launch/merge").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
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
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/launch/51824cc1553de743b4e5aa2c/finish").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
	}

	@Test
	public void forceFinishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setStatus(Status.PASSED.name());
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/launch/5187cba4553d2fdd93969fcd/stop").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
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

	@Test
	public void bulkDeleteLaunches() throws Exception {
		List<String> toDelete = asList("89224678053de743b3e5aa3e", "51824cc1323de743b3e5aa2c");
		mvcMock.perform(delete(PROJECT_BASE_URL + "/launch?ids=" + toDelete.stream().collect(joining(","))).contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));
		List<Launch> launches = launchRepository.find(toDelete);
		assertTrue(launches.isEmpty());
	}

	@Test
	public void bulkMoveToDebug() throws Exception {
		final List<String> ids = launchRepository.findLaunchIdsByProjectId(USER_PROJECT)
				.stream()
				.filter(it -> it.getMode() == DEFAULT)
				.map(Launch::getId)
				.collect(toList());
		final Map<String, UpdateLaunchRQ> entities = ids.stream().collect(toMap(it -> it, it -> {
			final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
			updateLaunchRQ.setMode(DEBUG);
			return updateLaunchRQ;
		}));
		final BulkRQ<UpdateLaunchRQ> bulkRQ = new BulkRQ<>();
		bulkRQ.setEntities(entities);
		mvcMock.perform(put(PROJECT_BASE_URL + "/launch/update").principal(authentication())
				.content(objectMapper.writeValueAsBytes(bulkRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
		launchRepository.find(ids).forEach(it -> assertTrue(it.getMode() == DEBUG));
	}

	@Test
	public void getLaunches() throws Exception {
		mvcMock.perform(get(PROJECT_BASE_URL + "/launch?page.page=1&page.size=50&page.sort=statistics$defects$product_bug,ASC").contentType(
				APPLICATION_JSON).principal(authentication())).andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return ADMINISTRATOR;
	}
}