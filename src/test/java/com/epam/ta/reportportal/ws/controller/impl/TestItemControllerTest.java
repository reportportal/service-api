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
import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.AddExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Dzmitry_Kavalets
 */
public class TestItemControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@SuppressWarnings("deprecation")
	@Test
	public void startRootItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("51824cc1553de743b3e5aa2c");
		rq.setName("RootItem");
		rq.setType("SUITE");
		rq.setParameters(getParameters());
		rq.setUniqueId(UUID.randomUUID().toString());
		rq.setStartTime(new Date(2014, 5, 5));
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/item").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))
				.principal(authentication())).andExpect(status().isCreated());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void startChildItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("51824cc1553de743b3e5aa2c");
		rq.setName("ChildItem");
		rq.setType("TEST");
		rq.setParameters(getParameters());
		rq.setStartTime(new Date(2014, 5, 6));
		this.mvcMock.perform(post(PROJECT_BASE_URL + "/item/44524cc1553de743b3e5aa30").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().isCreated());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void finishTestItemPositive() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(new Date(2014, 5, 7));
		rq.setStatus("PASSED");
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/item/44524cc1553de753b3e5bb2f").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getTestItemPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/item/44524cc1553de743b3e5aa30").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getTestItemsPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/item").principal(authentication())).andExpect(status().is(200));
	}

	@Test
	public void getTestItemsPredefined() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/item?predefined_filter=collapsed").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getTestItemsPredefinedUnknown() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/item?predefined_filter=unknown").principal(authentication()))
				.andExpect(status().is(400));
	}

	@Test
	public void deleteTestItemPositive() throws Exception {
		this.mvcMock.perform(delete(PROJECT_BASE_URL + "/item/44534cc1663de743b3e5aa33").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void getItemHistoryPositive() throws Exception {
		this.mvcMock.perform(get(PROJECT_BASE_URL + "/item/history?ids=44524cc1553de743b3e5aa30").principal(authentication()))
				.andExpect(status().is(200));
	}

	@Test
	public void defineTestItemIssueTypePositive() throws Exception {

		// it's possible to update an issue type using value instead of locator
		final String id = "44534cc1553de743b3e5aa33";
		DefineIssueRQ rq = new DefineIssueRQ();
		IssueDefinition issueDefinition = new IssueDefinition();
		issueDefinition.setId(id);
		Issue issue = new Issue();
		issue.setIssueType("PRODUCT_BUG");
		issue.setIgnoreAnalyzer(false);
		issueDefinition.setIssue(issue);
		rq.setIssues(Collections.singletonList(issueDefinition));
		mvcMock.perform(put(PROJECT_BASE_URL + "/item").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
		List<Activity> activities = activityRepository.findByLoggedObjectRef(id);
		assertNotNull(activities);
		assertFalse(activities.isEmpty());
		assertTrue(isHistoryPresent(activities, "To Investigate", "Product Bug"));

		issue.setIssueType("AB002");
		mvcMock.perform(put(PROJECT_BASE_URL + "/item").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
		List<Activity> newActivities = activityRepository.findByLoggedObjectRef(id);
		assertNotNull(newActivities);
		assertFalse(newActivities.isEmpty());
		assertTrue(isHistoryPresent(newActivities, "Product Bug", "Automation Bug #2"));
	}

	@Test
	public void updateTestItemPositive() throws Exception {
		UpdateTestItemRQ rq = new UpdateTestItemRQ();
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/item/44524cc1553de743b3e5aa30/update").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void addExternalIssuesPositive() throws Exception {
		AddExternalIssueRQ rq = new AddExternalIssueRQ();
		List<Issue.ExternalSystemIssue> issues = Lists.newArrayList();
		List<String> testItemIds = Lists.newArrayList();
		rq.setExternalSystemId("54958aec4e84859227150765");
		rq.setTestItemIds(testItemIds);
		rq.setIssues(issues);
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/item/issue/add").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
	}

	@Test
	public void getAllTags() throws Exception {
		this.mvcMock.perform(
				get(PROJECT_BASE_URL + "/item/tags?launch=51824cc1553de743b3e5aa2c&filter.cnt.tags=tags").principal(authentication())
						.contentType(APPLICATION_JSON)).andExpect(status().is(200));
	}

	@Test
	public void replaceExternalSystemIssue() throws Exception {
		final AddExternalIssueRQ rq = new AddExternalIssueRQ();
		final String testItemId = "44524cc1553de753b3e5cc2f";
		final String newUrl = "https://jira.epam.com/NEWTICKET1";
		rq.setExternalSystemId("54958aec4e84859227150765");
		rq.setTestItemIds(Collections.singletonList(testItemId));
		final Issue.ExternalSystemIssue issue = new Issue.ExternalSystemIssue();
		issue.setTicketId("TICKET1");
		issue.setUrl(newUrl);
		rq.setIssues(Collections.singletonList(issue));
		this.mvcMock.perform(put(PROJECT_BASE_URL + "/item/issue/add").principal(authentication())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().is(200));
		final String url = testItemRepository.findOne(testItemId).getIssue().getExternalSystemIssues().iterator().next().getUrl();
		assertEquals(newUrl, url);
	}

	@Override
	protected Authentication authentication() {
		return AuthConstants.ADMINISTRATOR;
	}

	private boolean isHistoryPresent(List<Activity> activities, String oldValue, String newValue) {
		return activities.stream()
				.map(Activity::getHistory)
				.flatMap(Collection::stream)
				.anyMatch(
						it -> it.getField().equals("issueType") && it.getOldValue().equals(oldValue) && it.getNewValue().equals(newValue));
	}

	private List<ParameterResource> getParameters() {
		ParameterResource parameters = new ParameterResource();
		parameters.setKey("CardNumber");
		parameters.setValue("4444333322221111");
		ParameterResource parameters1 = new ParameterResource();
		parameters1.setKey("Stars");
		parameters1.setValue("2 stars");
		return ImmutableList.<ParameterResource>builder().add(parameters).add(parameters1).build();
	}
}
