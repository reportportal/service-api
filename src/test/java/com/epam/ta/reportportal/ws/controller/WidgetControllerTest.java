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

import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/shareable/shareable-fill.sql")
class WidgetControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WidgetRepository widgetRepository;

	@Test
	void createWidgetPositive() throws Exception {
		WidgetRQ rq = new WidgetRQ();
		rq.setName("widget");
		rq.setDescription("description");
		rq.setWidgetType("oldLineChart");
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(Collections.singletonList("statistics$executions$passed"));
		contentParameters.setItemsCount(50);
		rq.setFilterIds(Collections.singletonList(3L));
		rq.setContentParameters(contentParameters);
		rq.setShare(true);
		final MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/widget").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		final EntryCreatedRS entryCreatedRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EntryCreatedRS.class);
		final Optional<Widget> optionalWidget = widgetRepository.findById(entryCreatedRS.getId());
		assertTrue(optionalWidget.isPresent());
		assertEquals("widget", optionalWidget.get().getName());
		assertEquals("description", optionalWidget.get().getDescription());
	}

	@Test
	void getWidgetPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/widget/10").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void updateWidgetPositive() throws Exception {
		final WidgetRQ rq = new WidgetRQ();
		rq.setName("updated");
		rq.setDescription("updated");
		rq.setWidgetType("activityStream");
		rq.setShare(false);
		final ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(Arrays.asList("number", "start_time", "user"));
		contentParameters.setItemsCount(50);
		rq.setContentParameters(contentParameters);
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/widget/12").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		final Optional<Widget> optionalWidget = widgetRepository.findById(12L);
		assertTrue(optionalWidget.isPresent());
		assertEquals("updated", optionalWidget.get().getName());
		assertEquals("updated", optionalWidget.get().getDescription());
	}

	@Test
	void updateNonExistingWidget() throws Exception {
		WidgetRQ rq = new WidgetRQ();
		rq.setName("name");
		rq.setWidgetType("oldLineChart");
		rq.setShare(false);
	    var contextParams =	new ContentParameters();
	    contextParams.setItemsCount(1);
		contextParams.setContentFields(Collections.singletonList("test"));
		rq.setContentParameters(contextParams);
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/widget/100").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	void updateWidgetWithDuplicatedName() throws Exception {
		final WidgetRQ rq = new WidgetRQ();
		rq.setName("LAUNCH STATISTICS");
		rq.setDescription("updated");
		rq.setWidgetType("activityStream");
		rq.setShare(false);
		final ContentParameters contentParameters = new ContentParameters();
		contentParameters.setContentFields(Arrays.asList("number", "start_time", "user"));
		contentParameters.setItemsCount(50);
		rq.setContentParameters(contentParameters);
		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/widget/5").with(token(oAuthHelper.getSuperadminToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isConflict());
	}

	@Test
	void getSharedWidgetsListPositive() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/shared").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void searchSharedWidgetsListPositive() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/shared/search?term=ch").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getWidgetNamesPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/widget/names/all").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().is(200));
	}

	@Test
	void getWidgetPreview() throws Exception {
		WidgetPreviewRQ request = new WidgetPreviewRQ();
		request.setWidgetType("launchStatistics");
		final ContentParameters contentParameters = new ContentParameters();
		final HashMap<String, Object> widgetOptions = new HashMap<>();
		widgetOptions.put("timeline", "WEEK");
		contentParameters.setWidgetOptions(widgetOptions);
		contentParameters.setItemsCount(20);
		contentParameters.setContentFields(Arrays.asList(
				"statistics$executions$total",
				"statistics$executions$passed",
				"statistics$executions$failed",
				"statistics$executions$skipped"
		));
		request.setContentParameters(contentParameters);
		request.setFilterIds(Collections.singletonList(4L));

		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/widget/preview").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Sql("/db/widget/launch-statistics.sql")
	@Test
	void getLaunchStatisticsWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launch stats"))
				.andExpect(jsonPath("$.widgetType").value("launchStatistics"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$automation_bug$ab001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$product_bug$pb001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$to_investigate$ti001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$failed").value("3"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$passed").value("2"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/not-passed.sql")
	@Test
	void getNotPassedWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("not passed"))
				.andExpect(jsonPath("$.widgetType").value("notPassed"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].values.*").value("60.0"));
	}

	@Sql("/db/widget/not-passed.sql")
	@Test
	void getEmptyContentNotPassedWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/launches-comparison-chart.sql")
	@Test
	void getLaunchesComparisonWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launch comparison"))
				.andExpect(jsonPath("$.widgetType").value("launchesComparisonChart"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$automation_bug$ab001").value("33.33"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$product_bug$pb001").value("33.33"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$to_investigate$ti001").value("33.33"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$failed").value("60.0"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$passed").value("40.0"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5.0"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$defects$product_bug$pb001").value("33.33"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$defects$to_investigate$ti001").value("66.67"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$executions$failed").value("60.0"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$executions$passed").value("20.0"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$executions$skipped").value("20.0"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$executions$total").value("5.0"));
	}

	@Sql("/db/widget/launches-comparison-chart.sql")
	@Test
	void getEmptyContentLaunchesComparisonWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launch comparison"))
				.andExpect(jsonPath("$.widgetType").value("launchesComparisonChart"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/launches-duration-chart.sql")
	@Test
	void getLaunchesDurationWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launches duration"))
				.andExpect(jsonPath("$.widgetType").value("launchesDurationChart"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].duration").value("540000"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].duration").value("660000"));
	}

	@Sql("/db/widget/launches-duration-chart.sql")
	@Test
	void getEmptyContentLaunchesDurationWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launches duration"))
				.andExpect(jsonPath("$.widgetType").value("launchesDurationChart"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/bug-trend.sql")
	@Test
	void getBugTrendWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("bug trend"))
				.andExpect(jsonPath("$.widgetType").value("bugTrend"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$automation_bug$total").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$product_bug$total").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$to_investigate$total").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.total").value("3"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$defects$product_bug$total").value("1"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$defects$to_investigate$total").value("2"))
				.andExpect(jsonPath("$.content.result[1].values.total").value("3"));
	}

	@Sql("/db/widget/launches-table.sql")
	@Test
	void getLaunchesTableWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launches table"))
				.andExpect(jsonPath("$.widgetType").value("launchesTable"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$skipped").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.status").value("FAILED"))
				.andExpect(jsonPath("$.content.result[0].values.description").value("desc"))
				.andExpect(jsonPath("$.content.result[0].values.user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$failed").value("3"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$passed").value("1"))
				.andExpect(jsonPath("$.content.result[0].attributes[0].value").value("value1"))
				.andExpect(jsonPath("$.content.result[0].attributes[1].value").value("value"))
				.andReturn();
	}

	@Sql("/db/widget/launches-table.sql")
	@Test
	void getEmptyContentLaunchesTableWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("launches table"))
				.andExpect(jsonPath("$.widgetType").value("launchesTable"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/top-test-cases.sql")
	@Test
	void getTopTestCasesWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("top test cases"))
				.andExpect(jsonPath("$.widgetType").value("topTestCases"))
				.andExpect(jsonPath("$.content.latestLaunch.name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].name").value("test item 5"))
				.andExpect(jsonPath("$.content.result[0].total").value("1"))
				.andExpect(jsonPath("$.content.result[1].name").value("test item 2"))
				.andExpect(jsonPath("$.content.result[1].total").value("1"))
				.andExpect(jsonPath("$.content.result[2].name").value("test item 3"))
				.andExpect(jsonPath("$.content.result[2].total").value("1"));
	}

	@Sql("/db/widget/top-test-cases.sql")
	@Test
	void getEmptyContentTopTestCasesWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("top test cases"))
				.andExpect(jsonPath("$.widgetType").value("topTestCases"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/top-test-cases.sql")
	@Test
	void getTopTestCasesWidgetWithNotExistLaunch() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/top-test-cases.sql")
	@Test
	void getTopTestCasesIncludeMethodsWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("top test cases"))
				.andExpect(jsonPath("$.widgetType").value("topTestCases"))
				.andExpect(jsonPath("$.content.latestLaunch.name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].name").value("test item 5"))
				.andExpect(jsonPath("$.content.result[0].total").value("1"))
				.andExpect(jsonPath("$.content.result[1].name").value("test item 2"))
				.andExpect(jsonPath("$.content.result[1].total").value("1"))
				.andExpect(jsonPath("$.content.result[2].name").value("test item 3"))
				.andExpect(jsonPath("$.content.result[2].total").value("1"));
	}

	@Sql("/db/widget/flaky-test-cases.sql")
	@Test
	void getFlakyTestCasesWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("flaky test cases"))
				.andExpect(jsonPath("$.widgetType").value("flakyTestCases"))
				.andExpect(jsonPath("$.content.latestLaunch.name").value("test launch"))
				.andExpect(jsonPath("$.content.flaky[0].flakyCount").value("1"))
				.andExpect(jsonPath("$.content.flaky[0].itemName").value("test item 4"));
	}

	@Sql("/db/widget/flaky-test-cases.sql")
	@Test
	void getFlakyTestCasesWidgetWithNotExistLaunch() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Sql("/db/widget/flaky-test-cases.sql")
	@Test
	void getEmptyContentFlakyTestCasesWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("flaky test cases"))
				.andExpect(jsonPath("$.widgetType").value("flakyTestCases"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/flaky-test-cases.sql")
	@Test
	void getFlakyTestCasesWithIncludeMethodsWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("flaky test cases"))
				.andExpect(jsonPath("$.widgetType").value("flakyTestCases"))
				.andExpect(jsonPath("$.content.latestLaunch.name").value("test launch"))
				.andExpect(jsonPath("$.content.flaky[0].flakyCount").value("1"))
				.andExpect(jsonPath("$.content.flaky[0].itemName").value("test item 4"));
	}

	@Sql("/db/widget/cases-trend.sql")
	@Test
	void getCasesTrendWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("cases trend"))
				.andExpect(jsonPath("$.widgetType").value("casesTrend"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].number").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].number").value("2"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/cases-trend.sql")
	@Test
	void getCasesTrendWidgetWithTimeline() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/5").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("cases trend"))
				.andExpect(jsonPath("$.widgetType").value("casesTrend"))
				.andExpect(jsonPath("$.content.result.*.name").value("test launch"))
				.andExpect(jsonPath("$.content.result.*.number").value(2))
				.andExpect(jsonPath("$.content.result.*.values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/cases-trend.sql")
	@Test
	void getEmptyContentCasesTrendWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/6").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("cases trend"))
				.andExpect(jsonPath("$.widgetType").value("casesTrend"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/cases-trend.sql")
	@Test
	void getCasesTrendWidgetWithWrongTimeLineOption() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/7").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("cases trend"))
				.andExpect(jsonPath("$.widgetType").value("casesTrend"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].number").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].number").value("2"))
				.andExpect(jsonPath("$.content.result[1].values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/cases-trend.sql")
	@Test
	void getCasesTrendWidgetWithDescOrdering() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/8").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("cases trend"))
				.andExpect(jsonPath("$.widgetType").value("casesTrend"))
				.andExpect(jsonPath("$.content.result.*.name").value("test launch"))
				.andExpect(jsonPath("$.content.result.*.number").value(2))
				.andExpect(jsonPath("$.content.result.*.values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/passing-rate-per-launch.sql")
	@Test
	void getPassingRatePerLaunchWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("passing rate per launch"))
				.andExpect(jsonPath("$.widgetType").value("passingRatePerLaunch"))
				.andExpect(jsonPath("$.content.result.passed").value("1"))
				.andExpect(jsonPath("$.content.result.total").value("5"))
				.andReturn();
	}

	@Sql("/db/widget/passing-rate-per-launch.sql")
	@Test
	void getEmptyContentPassingRatePerLaunchWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("passing rate per launch"))
				.andExpect(jsonPath("$.widgetType").value("passingRatePerLaunch"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/passing-rate-per-launch.sql")
	@Test
	void getPassingRatePerLaunchWidgetWithNotExistLaunchName() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Sql("/db/widget/passing-rate-summary.sql")
	@Test
	void getPassingRateSummaryWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("passing rate summary"))
				.andExpect(jsonPath("$.widgetType").value("passingRateSummary"))
				.andExpect(jsonPath("$.content.result.passed").value("3"))
				.andExpect(jsonPath("$.content.result.total").value("10"));
	}

	@Sql("/db/widget/passing-rate-summary.sql")
	@Test
	void getEmptyContentPassingRateSummaryWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/old-line-chart.sql")
	@Test
	void getOldLineChartWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("old line chart"))
				.andExpect(jsonPath("$.widgetType").value("oldLineChart"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$automation_bug$ab001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$product_bug$pb001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$to_investigate$ti001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$failed").value("3"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$passed").value("2"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/old-line-chart.sql")
	@Test
	void getEmptyContentOldLineChartWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("old line chart"))
				.andExpect(jsonPath("$.widgetType").value("oldLineChart"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/old-line-chart.sql")
	@Test
	void getOldLineChartWithTimeLineWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/5").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("old line chart"))
				.andExpect(jsonPath("$.widgetType").value("oldLineChart"))
				.andExpect(jsonPath("$.content.result.*.values.statistics$defects$automation_bug$ab001").value("1.0"))
				.andExpect(jsonPath("$.content.result.*.values.statistics$defects$product_bug$pb001").value("1.0"))
				.andExpect(jsonPath("$.content.result.*.values.statistics$defects$automation_bug$ab001").value("1.0"))
				.andExpect(jsonPath("$.content.result.*.values.statistics$executions$failed").value("3.0"))
				.andExpect(jsonPath("$.content.result.*.values.statistics$executions$passed").value("2.0"))
				.andExpect(jsonPath("$.content.result.*.values.statistics$executions$total").value("5.0"));
	}

	@Sql("/db/widget/old-line-chart.sql")
	@Test
	void getEmptyContentOldLineChartWithTimeLineWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/6").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("old line chart"))
				.andExpect(jsonPath("$.widgetType").value("oldLineChart"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/old-line-chart.sql")
	@Test
	void getOldLineChartWidgetWithIncorrectTimeLine() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/7").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("old line chart"))
				.andExpect(jsonPath("$.widgetType").value("oldLineChart"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/investigated-trend.sql")
	@Test
	void getInvestigatedTrendWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("investigated trend"))
				.andExpect(jsonPath("$.widgetType").value("investigatedTrend"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].number").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.toInvestigate").value("33.33"))
				.andExpect(jsonPath("$.content.result[0].values.investigated").value("66.67"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].number").value("2"))
				.andExpect(jsonPath("$.content.result[1].values.toInvestigate").value("66.67"))
				.andExpect(jsonPath("$.content.result[1].values.investigated").value("33.33"));
	}

	//Waiting for fix
	@Disabled
	@Sql("/db/widget/investigated-trend.sql")
	@Test
	void getInvestigatedTrendWidgetWithTimeline() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("investigated trend"))
				.andExpect(jsonPath("$.widgetType").value("investigatedTrend"));
	}

	@Sql("/db/widget/unique-bug-table.sql")
	@Test
	void getUniqueBugTableWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("unique bug table"))
				.andExpect(jsonPath("$.widgetType").value("uniqueBugTable"))
				.andExpect(jsonPath("$.content.result.ticket1.submitter").value("superadmin"))
				.andExpect(jsonPath("$.content.result.ticket1.url").value("http:/example.com/ticket1"))
				.andExpect(jsonPath("$.content.result.ticket1.items[0].launchId").value(1))
				.andExpect(jsonPath("$.content.result.ticket1.items[0].itemName").value("test item 2"))
				.andExpect(jsonPath("$.content.result.ticket1.items[0].itemId").value(2))
				.andExpect(jsonPath("$.content.result.ticket1.items[0].attributes", hasSize(2)));
	}

	@Sql("/db/widget/unique-bug-table.sql")
	@Test
	void getEmptyContentUniqueBugTableWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("unique bug table"))
				.andExpect(jsonPath("$.widgetType").value("uniqueBugTable"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/most-time-consuming.sql")
	@Test
	void getMostTimeConsumingWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("most time consuming"))
				.andExpect(jsonPath("$.widgetType").value("mostTimeConsuming"))
				.andExpect(jsonPath("$.content.result[0].name").value("test item 3"))
				.andExpect(jsonPath("$.content.result[0].duration").value("337.0"))
				.andExpect(jsonPath("$.content.result[1].name").value("test item 5"))
				.andExpect(jsonPath("$.content.result[1].duration").value("251.0"))
				.andExpect(jsonPath("$.content.result[2].name").value("test item 2"))
				.andExpect(jsonPath("$.content.result[2].duration").value("192.0"))
				.andExpect(jsonPath("$.content.result[3].name").value("test item 1"))
				.andExpect(jsonPath("$.content.result[3].duration").value("165.0"))
				.andExpect(jsonPath("$.content.result[4].name").value("test item 4"))
				.andExpect(jsonPath("$.content.result[4].duration").value("87.0"));
	}

	@Sql("/db/widget/most-time-consuming.sql")
	@Test
	void getEmptyContentMostTimeConsumingWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("most time consuming"))
				.andExpect(jsonPath("$.widgetType").value("mostTimeConsuming"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/most-time-consuming.sql")
	@Test
	void getMostTimeConsumingWidgetWithNotExistLaunch() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/5").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Sql("/db/widget/most-time-consuming.sql")
	@Test
	void getMostTimeConsumingWidgetWithIncludeMethods() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/6").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("most time consuming"))
				.andExpect(jsonPath("$.widgetType").value("mostTimeConsuming"))
				.andExpect(jsonPath("$.content.result[0].name").value("test item 3"))
				.andExpect(jsonPath("$.content.result[0].duration").value("337.0"))
				.andExpect(jsonPath("$.content.result[1].name").value("test item 5"))
				.andExpect(jsonPath("$.content.result[1].duration").value("251.0"))
				.andExpect(jsonPath("$.content.result[2].name").value("test item 2"))
				.andExpect(jsonPath("$.content.result[2].duration").value("192.0"))
				.andExpect(jsonPath("$.content.result[3].name").value("test item 1"))
				.andExpect(jsonPath("$.content.result[3].duration").value("165.0"))
				.andExpect(jsonPath("$.content.result[4].name").value("test item 4"))
				.andExpect(jsonPath("$.content.result[4].duration").value("87.0"));
	}

	@Sql("/db/widget/overall-statistics.sql")
	@Test
	void getOverallStatisticsWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("overall statistics"))
				.andExpect(jsonPath("$.widgetType").value("overallStatistics"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$automation_bug$ab001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$product_bug$pb001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$to_investigate$ti001").value("1"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$failed").value("3"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$passed").value("2"))
				.andExpect(jsonPath("$.content.result[0].values.statistics$executions$total").value("5"));
	}

	@Sql("/db/widget/overall-statistics.sql")
	@Test
	void getEmptyContentOverallStatisticsWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("overall statistics"))
				.andExpect(jsonPath("$.widgetType").value("overallStatistics"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/activity-stream.sql")
	@Test
	void getActivityStreamWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("activity stream"))
				.andExpect(jsonPath("$.widgetType").value("activityStream"))
				.andExpect(jsonPath("$.content.result[0].user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[0].actionType").value("startLaunch"))
				.andExpect(jsonPath("$.content.result[0].objectType").value("LAUNCH"))
				.andExpect(jsonPath("$.content.result[1].user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[1].actionType").value("updateItem"))
				.andExpect(jsonPath("$.content.result[1].objectType").value("ITEM"))
				.andExpect(jsonPath("$.content.result[2].user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[2].actionType").value("deleteLaunch"))
				.andExpect(jsonPath("$.content.result[2].objectType").value("LAUNCH"));
	}

	@Sql("/db/widget/activity-stream.sql")
	@Test
	void getEmptyContentActivityStreamWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("activity stream"))
				.andExpect(jsonPath("$.widgetType").value("activityStream"))
				.andExpect(jsonPath("$.content").isEmpty());
	}

	@Sql("/db/widget/activity-stream.sql")
	@Test
	void getActivityStreamWidgetWithNotExistUser() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isNotFound());
	}

	@Sql("/db/widget/activity-stream.sql")
	@Test
	void getActivityStreamWidgetWithEmptyUserOption() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("activity stream"))
				.andExpect(jsonPath("$.widgetType").value("activityStream"))
				.andExpect(jsonPath("$.content.result[0].user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[0].actionType").value("startLaunch"))
				.andExpect(jsonPath("$.content.result[0].objectType").value("LAUNCH"))
				.andExpect(jsonPath("$.content.result[1].user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[1].actionType").value("updateItem"))
				.andExpect(jsonPath("$.content.result[1].objectType").value("ITEM"))
				.andExpect(jsonPath("$.content.result[2].user").value("superadmin"))
				.andExpect(jsonPath("$.content.result[2].actionType").value("deleteLaunch"))
				.andExpect(jsonPath("$.content.result[2].objectType").value("LAUNCH"));
	}

	@Sql("/db/widget/product-status.sql")
	@Test
	void getProductStatusGroupedByLaunchWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("product status"))
				.andExpect(jsonPath("$.widgetType").value("productStatus"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].number").value("1"))
				.andExpect(jsonPath("$.content.result[0].attributes").isNotEmpty())
				.andExpect(jsonPath("$.content.result[0].passingRate").value("40.0"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].number").value("2"))
				.andExpect(jsonPath("$.content.result[1].attributes").doesNotExist())
				.andExpect(jsonPath("$.content.result[1].passingRate").value("20.0"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$executions$passed").value("3"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$executions$skipped").value("1"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$defects$to_investigate$ti001").value("3"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$defects$product_bug$pb001").value("2"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$defects$automation_bug$ab001").value("1"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$executions$failed").value("6"))
				.andExpect(jsonPath("$.content.result[2].sum.statistics$executions$total").value("10"))
				.andExpect(jsonPath("$.content.result[2].averagePassingRate").value("30.0"));
	}

	@Sql("/db/widget/component-health-check.sql")
	@Test
	void getComponentHealthCheckContent() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL
				+ "/widget/multilevel/2?attributes=3.29.11.0,arch").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("health"))
				.andExpect(jsonPath("$.widgetType").value("componentHealthCheck"))
				.andExpect(jsonPath("$.content.result[0].attributeValue").value("android"))
				.andExpect(jsonPath("$.content.result[0].total").value("1"))
				.andExpect(jsonPath("$.content.result[0].passingRate").value("0.0"))
				.andExpect(jsonPath("$.content.result[1].attributeValue").value("ios"))
				.andExpect(jsonPath("$.content.result[1].total").value("1"))
				.andExpect(jsonPath("$.content.result[1].passingRate").value("0.0"));
	}

	@Sql("/db/widget/product-status.sql")
	@Test
	void getEmptyContentProductStatusGroupedByLaunchWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/5").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.name").value("product status"))
				.andExpect(jsonPath("$.widgetType").value("productStatus"))
				.andExpect(jsonPath("$.content").isEmpty());
	}
}