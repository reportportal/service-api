package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

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
		contentParameters.setContentFields(Arrays.asList("number", "name", "user", "statistics$defects$automation_bug$AB002"));
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
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/widget/100").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isNotFound());
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
				.andExpect(content().contentType("application/json;charset=UTF-8"))
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
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.name").value("not passed"))
				.andExpect(jsonPath("$.widgetType").value("notPassed"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].values.*").value("60.0"));
	}

	@Sql("/db/widget/launches-comparison-chart.sql")
	@Test
	void getLaunchesComparisonWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
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

	@Sql("/db/widget/launches-duration-chart.sql")
	@Test
	void getLaunchesDurationWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.name").value("launches duration"))
				.andExpect(jsonPath("$.widgetType").value("launchesDurationChart"))
				.andExpect(jsonPath("$.content.result[0].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[0].duration").value("540000"))
				.andExpect(jsonPath("$.content.result[1].name").value("test launch"))
				.andExpect(jsonPath("$.content.result[1].duration").value("660000"));
	}

	@Sql("/db/widget/bug-trend.sql")
	@Test
	void getBugTrendWidget() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
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
		MvcResult mvcResult = mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/widget/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andReturn();

		WidgetResource response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<WidgetResource>() {
		});

		System.out.println();
	}
}