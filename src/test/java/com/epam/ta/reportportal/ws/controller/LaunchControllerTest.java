/*
 * Copyright 2019 EPAM Systems
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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.DeleteBulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.attribute.UpdateItemAttributeRQ;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/launch/launch-fill.sql")
class LaunchControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private LaunchRepository launchRepository;

	@Test
	void happyCreateLaunch() throws Exception {
		String name = "some launch name";
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setDescription("some description");
		startLaunchRQ.setName(name);
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setMode(DEFAULT);
		startLaunchRQ.setAttributes(Sets.newHashSet(new ItemAttributesRQ("key", "value")));

		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/launch/").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(startLaunchRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated());
	}

	@Test
	void getSuggestedItemsAnalyzerNotDeployed() throws Exception {
		AnalyzeLaunchRQ analyzeLaunchRQ = new AnalyzeLaunchRQ();
		analyzeLaunchRQ.setLaunchId(1L);
		analyzeLaunchRQ.setAnalyzeItemsModes(Collections.singletonList("TO_INVESTIGATE"));
		analyzeLaunchRQ.setAnalyzerTypeName("autoAnalyzer");
		analyzeLaunchRQ.setAnalyzerHistoryMode("ALL");
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/launch/analyze").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(analyzeLaunchRQ))
				.contentType(APPLICATION_JSON))
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof ReportPortalException))
				.andExpect(result -> assertEquals(
						"Impossible interact with integration. There are no analyzer services are deployed.",
						result.getResolvedException().getMessage()
				));
	}

	@Test
	void updateLaunchPositive() throws Exception {
		UpdateLaunchRQ rq = new UpdateLaunchRQ();
		rq.setMode(DEFAULT);
		rq.setDescription("description");
		rq.setAttributes(Sets.newHashSet(new ItemAttributeResource("test", "test")));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/3/update").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
	}

	@Test
	void getLaunchPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/2").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	void getLaunchStringPositive() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/launch/4850a659-ac26-4a65-8ea4-a6756a57fb92").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void getLaunchUuidPositive() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/launch/uuid/4850a659-ac26-4a65-8ea4-a6756a57fb92").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void getDebugLaunches() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/launch/mode").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().is(200));
	}

	@Test
	void compareLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/compare?ids=1,2").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void mergeLaunchesPositive() throws Exception {
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
	void deleteLaunchPositive() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/launch/1").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void deleteLaunchNegative() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/launch/3").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(406));
	}

	@Test
	void getStatus() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/status?ids=1").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void finishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		finishExecutionRQ.setStatus(StatusEnum.PASSED.name());
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/befef834-b2ef-4acf-aea3-b5a5b15fd93c/finish").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
	}

	@Test
	void forceFinishLaunch() throws Exception {
		final FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		finishExecutionRQ.setStatus(StatusEnum.PASSED.name());
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/3/stop").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(finishExecutionRQ))).andExpect(status().is(200));
	}

	@Test
	void bulkForceFinish() throws Exception {
		final BulkRQ<Long, FinishExecutionRQ> bulkRQ = new BulkRQ<>();
		bulkRQ.setEntities(Stream.of(3L, 5L).collect(toMap(it -> it, it -> {
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
	void getAllOwners() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/owners?filter.cnt.user=def").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	void getAllLaunchNames() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/names?filter.cnt.name=test").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	void bulkDeleteLaunches() throws Exception {
		DeleteBulkRQ deleteBulkRQ = new DeleteBulkRQ();
		List<Long> ids = Lists.newArrayList(1L, 2L);
		deleteBulkRQ.setIds(ids);
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/launch").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(deleteBulkRQ))).andExpect(status().is(200));
		List<Launch> launches = launchRepository.findAllById(ids);
		assertTrue(launches.isEmpty());
	}

	@Test
	void bulkMoveToDebug() throws Exception {
		final List<Long> ids = launchRepository.findByFilter(Filter.builder()
				.withTarget(Launch.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ID, String.valueOf(2L)).build())
				.build()).stream().filter(it -> it.getMode() == LaunchModeEnum.DEFAULT).map(Launch::getId).collect(Collectors.toList());
		final Map<Long, UpdateLaunchRQ> entities = ids.stream().collect(toMap(it -> it, it -> {
			final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
			updateLaunchRQ.setMode(DEBUG);
			return updateLaunchRQ;
		}));
		final BulkRQ<Long, UpdateLaunchRQ> bulkRQ = new BulkRQ<>();
		bulkRQ.setEntities(entities);
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/update").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(bulkRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
		launchRepository.findAllById(ids).forEach(it -> assertSame(it.getMode(), LaunchModeEnum.DEBUG));
	}

	@Test
	void getLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL
				+ "/launch?page.page=1&page.size=50&page.sort=statistics$defects$product_bug$total,ASC").contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().is(200));
	}

	@Test
	void getLatestLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL
				+ "/launch/latest?page.page=1&page.size=10&page.sort=name,ASC").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void getAttributeKeys() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL
				+ "/launch/attribute/keys?filter.cnt.attributeKey=browser").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAttributeValues() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/launch/attribute/values?filter.eq.attributeKey=browser&filter.cnt.attributeValue=ch").with(
				token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectLaunches() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(4)));
	}

	@Test
	void export() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/1/report").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void bulkUpdateItemAttributes() throws Exception {
		BulkInfoUpdateRQ request = new BulkInfoUpdateRQ();
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L, 4L);
		request.setIds(launchIds);
		BulkInfoUpdateRQ.Description description = new BulkInfoUpdateRQ.Description();
		description.setAction(BulkInfoUpdateRQ.Action.CREATE);
		String comment = "created";
		description.setComment(comment);
		request.setDescription(description);
		UpdateItemAttributeRQ updateItemAttributeRQ = new UpdateItemAttributeRQ();
		updateItemAttributeRQ.setAction(BulkInfoUpdateRQ.Action.UPDATE);
		updateItemAttributeRQ.setFrom(new ItemAttributeResource("testKey", "testValue"));
		updateItemAttributeRQ.setTo(new ItemAttributeResource("updatedKey", "updatedValue"));
		request.setAttributes(Lists.newArrayList(updateItemAttributeRQ));

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/info").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		List<Launch> launches = launchRepository.findAllById(launchIds);
		launches.forEach(it -> launchRepository.refresh(it));

		launches.forEach(it -> {
			assertTrue(it.getAttributes()
					.stream()
					.noneMatch(attr -> "testKey".equals(attr.getKey()) && attr.getValue().equals("testValue") && !attr.isSystem()));
			assertTrue(it.getAttributes()
					.stream()
					.anyMatch(attr -> "updatedKey".equals(attr.getKey()) && attr.getValue().equals("updatedValue") && !attr.isSystem()));
			assertEquals(comment, it.getDescription());
		});
	}

	@Test
	void bulkCreateAttributes() throws Exception {
		BulkInfoUpdateRQ request = new BulkInfoUpdateRQ();
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L, 4L);
		request.setIds(launchIds);
		BulkInfoUpdateRQ.Description description = new BulkInfoUpdateRQ.Description();
		description.setAction(BulkInfoUpdateRQ.Action.UPDATE);
		String comment = "updated";
		description.setComment(comment);
		request.setDescription(description);
		UpdateItemAttributeRQ updateItemAttributeRQ = new UpdateItemAttributeRQ();
		updateItemAttributeRQ.setAction(BulkInfoUpdateRQ.Action.CREATE);
		updateItemAttributeRQ.setTo(new ItemAttributeResource("createdKey", "createdValue"));
		request.setAttributes(Lists.newArrayList(updateItemAttributeRQ));

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/info").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		List<Launch> launches = launchRepository.findAllById(launchIds);
		launches.forEach(it -> launchRepository.refresh(it));

		launches.forEach(it -> {
			assertTrue(it.getAttributes()
					.stream()
					.anyMatch(attr -> "createdKey".equals(attr.getKey()) && attr.getValue().equals("createdValue") && !attr.isSystem()));
			assertTrue(it.getDescription().length() > comment.length() && it.getDescription().contains(comment));
		});
	}

	@Test
	void bulkDeleteAttributes() throws Exception {
		BulkInfoUpdateRQ request = new BulkInfoUpdateRQ();
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L, 4L);
		request.setIds(launchIds);
		BulkInfoUpdateRQ.Description description = new BulkInfoUpdateRQ.Description();
		description.setAction(BulkInfoUpdateRQ.Action.CREATE);
		String comment = "created";
		description.setComment(comment);
		request.setDescription(description);
		UpdateItemAttributeRQ updateItemAttributeRQ = new UpdateItemAttributeRQ();
		updateItemAttributeRQ.setAction(BulkInfoUpdateRQ.Action.DELETE);
		updateItemAttributeRQ.setFrom(new ItemAttributeResource("testKey", "testValue"));
		request.setAttributes(Lists.newArrayList(updateItemAttributeRQ));

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/launch/info").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		List<Launch> launches = launchRepository.findAllById(launchIds);
		launches.forEach(it -> launchRepository.refresh(it));

		launches.forEach(it -> {
			assertTrue(it.getAttributes()
					.stream()
					.noneMatch(attr -> "testKey".equals(attr.getKey()) && attr.getValue().equals("testValue") && !attr.isSystem()));
			assertEquals(comment, it.getDescription());
		});
	}
}