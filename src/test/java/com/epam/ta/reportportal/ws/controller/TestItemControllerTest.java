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

import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRq;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/test-item/test-item-fill.sql")
class TestItemControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void startRootItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(1L);
		rq.setName("RootItem");
		rq.setType("SUITE");
		rq.setParameters(getParameters());
		rq.setUniqueId(UUID.randomUUID().toString());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/item").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	@Test
	void startChildItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(1L);
		rq.setName("ChildItem");
		rq.setType("TEST");
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/item/1").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	@Test
	void finishTestItemPositive() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("PASSED");
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/1").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getTestItemPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item/1").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getTestItemsPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item?filter.eq.launchId=1").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getTestItemBySpecifiedIds() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item/items?ids=1,2,3").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void deleteTestItemPositive() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/item/2").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteTestItemBySpecifiedIds() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/item?ids=2,3").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getItemHistoryPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item/history?ids=1").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void updateTestItemPositive() throws Exception {
		UpdateTestItemRQ rq = new UpdateTestItemRQ();
		rq.setDescription("updated");
		rq.setAttributes(Sets.newHashSet(new ItemAttributeResource("test", "test")));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/1/update").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void getAttributeKeys() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL
				+ "/item/attribute/keys?launch=1&filter.cnt.attributeKey=bro").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAttributeValues() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item/attribute/values?launch=1&filter.cnt.attributeValue=lin").with(token(
				oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void defineTestItemIssue() throws Exception {
		DefineIssueRQ rq = new DefineIssueRQ();
		IssueDefinition issueDefinition = new IssueDefinition();
		issueDefinition.setId(3L);
		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issueDefinition.setIssue(issue);
		rq.setIssues(Collections.singletonList(issueDefinition));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void linkExternalIssues() throws Exception {
		LinkExternalIssueRQ rq = new LinkExternalIssueRQ();
		rq.setTestItemIds(Collections.singletonList(3L));
		Issue.ExternalSystemIssue issue = new Issue.ExternalSystemIssue();
		issue.setBtsUrl("jira.com");
		issue.setBtsProject("project");
		issue.setSubmitter(2L);
		issue.setTicketId("ticket1");
		issue.setUrl("https://example.com/NEWTICKET1");
		rq.setIssues(Collections.singletonList(issue));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/issue/link").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	void unlinkExternalIssues() throws Exception {
		UnlinkExternalIssueRq rq = new UnlinkExternalIssueRq();
		rq.setTestItemIds(Collections.singletonList(3L));
		rq.setIssueIds(Collections.singletonList("ticket"));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/issue/unlink").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
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