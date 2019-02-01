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

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/launch/launch-fill.sql")
public class LaunchControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private LaunchRepository launchRepository;

	@Test
	public void happyCreateLaunch() throws Exception {
		String name = "some launch name";
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName(name);
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setMode(DEFAULT);

		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/launch/").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated());
	}

	@Test
	public void updateLaunchPositive() throws Exception {
		UpdateLaunchRQ rq = new UpdateLaunchRQ();
		rq.setMode(DEFAULT);
		rq.setDescription("description");
		rq.setAttributes(Sets.newHashSet(new ItemAttributeResource("test", "test")));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/3/update").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
	}

	@Test
	public void getLaunchPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/2").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void getDebugLaunches() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/launch/mode").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().is(200));
	}

	@Test
	public void compareLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/compare?ids=1,2").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void mergeLaunchesPositive() throws Exception {
		MergeLaunchesRQ rq = new MergeLaunchesRQ();
		HashSet<Long> set = new HashSet<>();
		set.add(1L);
		set.add(2L);
		rq.setLaunches(set);
		rq.setName("Merged");
		rq.setMergeStrategyType("BASIC");
		rq.setStartTime(new Date());
		rq.setEndTime(new Date());
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/launch/merge").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void deleteLaunchPositive() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/launch/1").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void deleteLaunchNegative() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/launch/3").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(406));
	}

	@Test
	public void getStatus() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/status?ids=1").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void finishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		finishExecutionRQ.setStatus(StatusEnum.PASSED.name());
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/3/finish").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
	}

	@Test
	public void forceFinishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		finishExecutionRQ.setStatus(StatusEnum.PASSED.name());
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/3/stop").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
	}

	@Test
	public void bulkForceFinish() throws Exception {
		final BulkRQ<FinishExecutionRQ> bulkRQ = new BulkRQ<>();
		bulkRQ.setEntities(LongStream.of(3L, 5L).boxed().collect(toMap(it -> it, it -> {
			FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
			finishExecutionRQ.setStatus(StatusEnum.PASSED.name());
			finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
			return finishExecutionRQ;
		})));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/stop").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(bulkRQ))).andExpect(status().isOk());
	}

	@Test
	public void getAllOwners() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/owners?filter.cnt.user=def").contentType(APPLICATION_JSON).with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void getAllLaunchNames() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/names?filter.cnt.name=test").contentType(APPLICATION_JSON).with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void bulkDeleteLaunches() throws Exception {
		List<Long> toDelete = asList(1L, 2L);
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/launch?ids=" + toDelete.stream().map(Object::toString).collect(Collectors.joining(","))).contentType(APPLICATION_JSON).with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
		List<Launch> launches = launchRepository.findAllById(toDelete);
		assertTrue(launches.isEmpty());
	}

	@Test
	public void bulkMoveToDebug() throws Exception {
		final List<Long> ids = launchRepository.findByFilter(Filter.builder()
				.withTarget(Launch.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(2L)).build())
				.build()).stream().filter(it -> it.getMode() == LaunchModeEnum.DEFAULT).map(Launch::getId).collect(Collectors.toList());
		final Map<Long, UpdateLaunchRQ> entities = ids.stream().collect(toMap(it -> it, it -> {
			final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
			updateLaunchRQ.setMode(DEBUG);
			return updateLaunchRQ;
		}));
		final BulkRQ<UpdateLaunchRQ> bulkRQ = new BulkRQ<>();
		bulkRQ.setEntities(entities);
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/update").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(bulkRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
		launchRepository.findAllById(ids).forEach(it -> assertSame(it.getMode(), LaunchModeEnum.DEBUG));
	}

	@Test
	@Ignore
	public void getLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch?page.page=1&page.size=50&page.sort=statistics$defects$product_bug$total,ASC").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	public void getLatestLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/latest?page.page=1&page.size=10&page.sort=name,ASC").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	public void getAttributeKeys() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/attribute/keys?filter.cnt.attributeKey=browser").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void getAttributeValues() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/attribute/values?filter.eq.attributeKey=browser&filter.cnt.attributeValue=ch").with(
				token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}
}