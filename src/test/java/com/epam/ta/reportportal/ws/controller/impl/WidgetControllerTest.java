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
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.widget.ContentParameters;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
public class WidgetControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private WidgetRepository widgetRepository;

	@Test
	public void createWidgetPositive() throws Exception {
		WidgetRQ rq = new WidgetRQ();
		rq.setName("widget");
		rq.setDescription("description");
		ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget("old_line_chart");
		contentParameters.setType("line_chart");
		contentParameters.setMetadataFields(Arrays.asList("number", "name"));
		contentParameters.setContentFields(Arrays.asList("number", "name", "user", "statistics$defects$automation_bug$AB002"));
		contentParameters.setItemsCount(50);
		rq.setFilterId("566e1f3818177ca344439d40");
		rq.setContentParameters(contentParameters);
		rq.setShare(true);
		final MvcResult mvcResult = mvcMock.perform(post(PROJECT_BASE_URL + "/widget").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		final EntryCreatedRS entryCreatedRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EntryCreatedRS.class);
		final Widget widget = widgetRepository.findOne(entryCreatedRS.getId());
		Assert.assertNotNull(widget);
		Assert.assertEquals("description", widget.getDescription());
	}

	@Test
	public void getWidgetPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f38").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void updateWidgetPositive() throws Exception {
		final WidgetRQ rq = new WidgetRQ();
		rq.setName("Most failure test-cases table new");
		rq.setDescription("description");
		rq.setFilterId("566e1f3818177ca344439d40");
		rq.setShare(true);
		final ContentParameters contentParameters = new ContentParameters();
		contentParameters.setGadget("old_line_chart");
		contentParameters.setType("table");
		contentParameters.setMetadataFields(Arrays.asList("number", "start_time"));
		contentParameters.setContentFields(Arrays.asList("number", "start_time", "user"));
		contentParameters.setItemsCount(50);
		rq.setContentParameters(contentParameters);
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f38").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(200));
		final Widget widget = widgetRepository.findOne("613e1f3818127ca356339f38");
		Assert.assertEquals("description", widget.getDescription());
	}

	@Test
	public void updateNonExistingWidget() throws Exception {
		WidgetRQ rq = new WidgetRQ();
		rq.setShare(false);
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/widget/non-existing").principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().is(404));
	}

	@Test
	public void getSharedWidgetsPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/names/shared").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getSharedWidgetsListPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/shared").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void searchSharedWidgetsListPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/shared/search?term=er").principal(authentication()))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.content", Matchers.hasSize(4)));
	}

	@Test
	public void getWidgetNamesPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/names/all").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getMostFailedTestCasesWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f38").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getTestCasesGrowthTrendWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f39").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLaunchesComparisonWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f40").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getInvestigatedTrendWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f41").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getUniqueBugsWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f42").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLineChartWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f43").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getNonPassedWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f44").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLaunchesTableWidget() throws Exception {
		mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f45").principal(authentication()))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.content.result[0].values.statistics$defects$product_bug").value("3"));
	}

	@Test
	public void getLaunchesDurationChartWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f46").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getLaunchStatisticsWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f47").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getOverallStatisticsWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f48").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getFailedCasesTrendWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f49").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getGrowthChartTimeLineWidget() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/widget/613e1f3818127ca356339f50").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}