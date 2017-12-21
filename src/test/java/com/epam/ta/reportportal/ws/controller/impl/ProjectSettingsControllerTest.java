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
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectSettingsControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private WidgetRepository widgetRepository;

	@Test
	public void createSubType() throws Exception {
		CreateIssueSubTypeRQ rq = new CreateIssueSubTypeRQ();
		rq.setTypeRef("PRODUCT_BUG");
		rq.setColor("color");
		rq.setLongName("LongName");
		rq.setShortName("name");
		MvcResult mvcResult = mvcMock.perform(post("/project1/settings/sub-type").contentType(APPLICATION_JSON)
				.principal(authentication())
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isCreated()).andReturn();
		EntryCreatedRS entryCreatedRS = new Gson().fromJson(mvcResult.getResponse().getContentAsString(), EntryCreatedRS.class);
		checkCustomDefectAdded(widgetRepository.findOne("613e1f3818127ca356339f47"), entryCreatedRS.getId());
		checkCustomDefectAdded(widgetRepository.findOne("613e1f3818127ca356339f45"), entryCreatedRS.getId());
	}

	@Test
	public void deleteSubType() throws Exception {
		String field = "statistics$defects$automation_bug$AB002";
		mvcMock.perform(delete("/project1/settings/sub-type/AB002").contentType(APPLICATION_JSON).principal(authentication()));
		Widget statisticsLineChart = widgetRepository.findOne("613e1f3818127ca356339f43");
		Widget launchesComparisonChart = widgetRepository.findOne("613e1f3818127ca356339f40");
		Widget launchesTable = widgetRepository.findOne("613e1f3818127ca356339f45");
		Widget mostFailure = widgetRepository.findOne("613e1f3818127ca356339f38");
		Widget investigatedPercentage = widgetRepository.findOne("613e1f3818127ca356339f41");
		Widget execAndIssueStats = widgetRepository.findOne("613e1f3818127ca356339f47");
		Widget overallStatistics = widgetRepository.findOne("613e1f3818127ca356339f48");
		Widget statisticsTrendChart = widgetRepository.findOne("613e1f3818127ca356339f51");
		checkCustomDefectFieldRemoved(statisticsTrendChart, field);
		checkCustomDefectFieldRemoved(overallStatistics, field);
		checkCustomDefectFieldRemoved(execAndIssueStats, field);
		checkCustomDefectFieldRemoved(investigatedPercentage, field);
		checkCustomDefectFieldRemoved(mostFailure, field);
		checkCustomDefectFieldRemoved(launchesTable, field);
		checkCustomDefectFieldRemoved(statisticsLineChart, field);
		checkCustomDefectFieldRemoved(launchesComparisonChart, field);
	}

	private static void checkCustomDefectFieldRemoved(Widget widget, String field) {
		Assert.assertFalse(widget.getContentOptions().getContentFields().contains(field));
	}

	private static void checkCustomDefectAdded(Widget widget, String subTypeId) {
		Assert.assertNotNull(widget);
		List<String> productBugs = widget.getContentOptions()
				.getContentFields()
				.stream()
				.filter(it -> it.contains("statistics$defects$product_bug$" + subTypeId))
				.collect(toList());
		Assert.assertEquals(1, productBugs.size());
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}
}