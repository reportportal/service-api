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

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.ParameterResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.attribute.UpdateItemAttributeRQ;
import com.epam.ta.reportportal.ws.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.item.LinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRQ;
import com.epam.ta.reportportal.ws.model.item.UpdateTestItemRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/test-item/test-item-fill.sql")
class TestItemControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Test
	void startRootItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid("a7b66ef2-db30-4db7-94df-f5f7786b398a");
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
	void startRootItemWithoutUuid() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("RootItem");
		rq.setType("SUITE");
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(SUPERADMIN_PROJECT_BASE_URL + "/item").contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isCreated());
	}

	@Test
	void startChildItemPositive() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("ChildItem");
		rq.setType("TEST");
		rq.setUniqueId(UUID.randomUUID().toString());
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(
				DEFAULT_PROJECT_BASE_URL + "/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	@Test
	void startChildItemWithoutUuid() throws Exception {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchUuid("a7b66ef2-db30-4db7-94df-f5f7786b398a");
		rq.setName("ChildItem");
		rq.setType("TEST");
		rq.setParameters(getParameters());
		rq.setStartTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(post(
				DEFAULT_PROJECT_BASE_URL + "/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isCreated());
	}

	@Test
	void finishTestItemPositive() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("PASSED");
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").content(objectMapper.writeValueAsBytes(
				rq)).contentType(APPLICATION_JSON).with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void finishRootTestItemWithoutStatus() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").content(objectMapper.writeValueAsBytes(
				rq)).contentType(APPLICATION_JSON).with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void finishTestItemWithFailedStatus() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");
		Issue issue = new Issue();
		issue.setIssueType("pb001");
		rq.setIssue(issue);
		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void finishTestItemWithoutIssueType() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");
		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getTestItemPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item/1").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getTestItemStringPositive() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/item/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getTestItemUuidPositive() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/item/uuid/0f7ca5bc-cfae-4cc1-9682-e59c2860131e").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
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
	void getItemHistoryByParentIdPositive() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/item/history?filter.eq.parentId=1&historyDepth=3").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getItemHistoryByLaunchIdPositive() throws Exception {
		mockMvc.perform(get(
				SUPERADMIN_PROJECT_BASE_URL + "/item/history?filter.eq.launchId=1&historyDepth=3").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getItemHistoryByFilterIdPositive() throws Exception {
		mockMvc.perform(get(
				DEFAULT_PROJECT_BASE_URL + "/item/history?filterId=1&launchesLimit=10&historyDepth=3").with(token(oAuthHelper.getDefaultToken())))
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
	void getTickets() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/item/ticket/ids?launch=1&term=ticket").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAttributeKeys() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL
				+ "/item/attribute/keys?launch=1&filter.cnt.attributeKey=bro").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAttributeKeysForProject() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL
				+ "/item/attribute/keys/all?filterId=1&launchesLimit=600&isLatest=false&filter.cnt.attributeKey=bro").with(token(oAuthHelper
				.getDefaultToken()))).andExpect(status().isOk());
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
	void defineTestItemIssueNegative() throws Exception {
		DefineIssueRQ rq = new DefineIssueRQ();
		IssueDefinition issueDefinition = new IssueDefinition();
		issueDefinition.setId(100L);
		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issueDefinition.setIssue(issue);
		rq.setIssues(Collections.singletonList(issueDefinition));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isBadRequest());
	}

	@Test
	void finishTestItemWithLinkedTicketsBadTicketId() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid("334d153c-8f9c-4dff-8627-47dd003bee0f");
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");

		Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
		ticket.setBtsUrl("jira.com");
		ticket.setBtsProject("project");
		ticket.setUrl("https://example.com/NEWTICKET1");

		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issue.setExternalSystemIssues(Sets.newHashSet(ticket));

		rq.setIssue(issue);

		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void finishTestItemWithLinkedTicketsBadBtsUrl() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid("334d153c-8f9c-4dff-8627-47dd003bee0f");
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");

		Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
		ticket.setBtsProject("project");
		ticket.setTicketId("ticket1");
		ticket.setUrl("https://example.com/NEWTICKET1");

		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issue.setExternalSystemIssues(Sets.newHashSet(ticket));

		rq.setIssue(issue);

		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}


	@Test
	void finishTestItemWithLinkedTicketsBadBtsProject() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid("334d153c-8f9c-4dff-8627-47dd003bee0f");
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");

		Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
		ticket.setBtsUrl("jira.com");
		ticket.setTicketId("ticket1");
		ticket.setUrl("https://example.com/NEWTICKET1");

		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issue.setExternalSystemIssues(Sets.newHashSet(ticket));

		rq.setIssue(issue);

		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}


	@Test
	void finishTestItemWithLinkedTicketsBadUrl() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid("334d153c-8f9c-4dff-8627-47dd003bee0f");
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");

		Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
		ticket.setBtsUrl("jira.com");
		ticket.setBtsProject("project");
		ticket.setTicketId("ticket1");

		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issue.setExternalSystemIssues(Sets.newHashSet(ticket));

		rq.setIssue(issue);

		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void finishTestItemWithEmptyLinkedTickets() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid("334d153c-8f9c-4dff-8627-47dd003bee0f");
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");

		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issue.setExternalSystemIssues(Sets.newHashSet());

		rq.setIssue(issue);

		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}


	@Test
	void finishTestItemWithLinkedTickets() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid("334d153c-8f9c-4dff-8627-47dd003bee0f");
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");

		Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
		ticket.setBtsUrl("jira.com");
		ticket.setBtsProject("project");
		ticket.setTicketId("ticket1");
		ticket.setUrl("https://example.com/NEWTICKET1");

		Issue issue = new Issue();
		issue.setIssueType("pb001");
		issue.setIgnoreAnalyzer(false);
		issue.setExternalSystemIssues(Sets.newHashSet(ticket));

		rq.setIssue(issue);

		mockMvc.perform(put(
				SUPERADMIN_PROJECT_BASE_URL + "/item/3ab067e5-537b-45ff-9605-843ab695c96a").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void linkExternalIssues() throws Exception {
		LinkExternalIssueRQ rq = new LinkExternalIssueRQ();
		rq.setTestItemIds(Collections.singletonList(3L));
		Issue.ExternalSystemIssue issue = new Issue.ExternalSystemIssue();
		issue.setBtsUrl("jira.com");
		issue.setBtsProject("project");
		issue.setTicketId("ticket1");
		issue.setUrl("https://example.com/NEWTICKET1");
		rq.setIssues(Collections.singletonList(issue));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/issue/link").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	void linkExternalIssueNegative() throws Exception {
		LinkExternalIssueRQ rq = new LinkExternalIssueRQ();
		rq.setTestItemIds(Collections.singletonList(2L));
		Issue.ExternalSystemIssue issue = new Issue.ExternalSystemIssue();
		issue.setBtsUrl("jira.com");
		issue.setBtsProject("project");
		issue.setTicketId("ticket1");
		issue.setUrl("https://example.com/NEWTICKET1");
		rq.setIssues(Collections.singletonList(issue));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/issue/link").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	void unlinkExternalIssues() throws Exception {
		UnlinkExternalIssueRQ rq = new UnlinkExternalIssueRQ();
		rq.setTestItemIds(Collections.singletonList(3L));
		rq.setTicketIds(Collections.singletonList("ticket"));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/issue/unlink").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void unlinkExternalIssuesNegative() throws Exception {
		UnlinkExternalIssueRQ rq = new UnlinkExternalIssueRQ();
		rq.setTestItemIds(Collections.singletonList(2L));
		rq.setTicketIds(Collections.singletonList("ticket"));
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/issue/unlink").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isBadRequest());
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

	@Test
	void getItemsByAdmin() throws Exception {
		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/item/items?ids=1,2,4").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)));
	}

	@Sql("/db/test-item/item-change-status-from-passed.sql")
	@Test
	void changeStatusFromPassedToFailed() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("failed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);

		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.FAILED, updatedItem.get().getItemResults().getStatus());
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(2)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-passed.sql")
	@Test
	void changeStatusFromPassedToSkipped() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("skipped");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.SKIPPED, updatedItem.get().getItemResults().getStatus());
		assertEquals(
				TestItemIssueGroup.TO_INVESTIGATE,
				updatedItem.get().getItemResults().getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
		);
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(2)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-passed.sql")
	@Test
	void changeStatusFromPassedToSkippedWithoutIssue() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("skipped");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/9/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(9L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.SKIPPED, updatedItem.get().getItemResults().getStatus());
		assertNull(updatedItem.get().getItemResults().getIssue());
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(2)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-passed.sql")
	@Test
	void finishTestItemWithFinishedParent() throws Exception {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setLaunchUuid(UUID.randomUUID().toString());
		rq.setEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant()));
		rq.setStatus("FAILED");
		Issue issue = new Issue();
		issue.setIssueType("pb001");
		rq.setIssue(issue);

		Optional<TestItem> updatedItem = testItemRepository.findById(11L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.IN_PROGRESS, updatedItem.get().getItemResults().getStatus());

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/uuid_s_2_9").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());

		updatedItem = testItemRepository.findById(11L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.FAILED, updatedItem.get().getItemResults().getStatus());
		assertEquals(
				TestItemIssueGroup.PRODUCT_BUG,
				updatedItem.get().getItemResults().getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
		);
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());
	}

	@Sql("/db/test-item/item-change-status-from-failed.sql")
	@Test
	void changeStatusFromFailedToPassed() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("passed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.PASSED, updatedItem.get().getItemResults().getStatus());
		assertNull(updatedItem.get().getItemResults().getIssue());
		assertEquals(StatusEnum.PASSED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.PASSED, launch.getStatus());

		verify(messageBus, times(2)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-failed.sql")
	@Test
	void changeStatusFromFailedToSkipped() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("skipped");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.SKIPPED, updatedItem.get().getItemResults().getStatus());
		assertEquals(
				TestItemIssueGroup.AUTOMATION_BUG,
				updatedItem.get().getItemResults().getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
		);
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(1)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-skipped.sql")
	@Test
	void changeStatusFromSkippedToFailed() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("failed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.FAILED, updatedItem.get().getItemResults().getStatus());
		assertEquals(
				TestItemIssueGroup.TO_INVESTIGATE,
				updatedItem.get().getItemResults().getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
		);
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(1)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-skipped.sql")
	@Test
	void changeStatusFromSkippedToPassed() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("passed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.PASSED, updatedItem.get().getItemResults().getStatus());
		assertNull(updatedItem.get().getItemResults().getIssue());
		assertEquals(StatusEnum.PASSED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.PASSED, launch.getStatus());

		verify(messageBus, times(2)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-interrupted.sql")
	@Test
	void changeStatusFromInterruptedToPassed() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("passed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.PASSED, updatedItem.get().getItemResults().getStatus());
		assertNull(updatedItem.get().getItemResults().getIssue());
		assertEquals(StatusEnum.PASSED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.PASSED, launch.getStatus());

		verify(messageBus, times(2)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-interrupted.sql")
	@Test
	void changeStatusFromInterruptedToSkipped() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("skipped");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.SKIPPED, updatedItem.get().getItemResults().getStatus());
		assertEquals(
				TestItemIssueGroup.TO_INVESTIGATE,
				updatedItem.get().getItemResults().getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
		);
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(1)).publishActivity(ArgumentMatchers.any());
	}

	@Sql("/db/test-item/item-change-status-from-interrupted.sql")
	@Test
	void changeStatusFromInterruptedToFailed() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("failed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/6/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		Optional<TestItem> updatedItem = testItemRepository.findById(6L);
		assertTrue(updatedItem.isPresent());
		assertEquals(StatusEnum.FAILED, updatedItem.get().getItemResults().getStatus());
		assertEquals(
				TestItemIssueGroup.TO_INVESTIGATE,
				updatedItem.get().getItemResults().getIssue().getIssueType().getIssueGroup().getTestItemIssueGroup()
		);
		assertEquals(StatusEnum.FAILED, updatedItem.get().getParent().getItemResults().getStatus());

		Launch launch = launchRepository.findById(updatedItem.get().getLaunchId()).get();
		assertEquals(StatusEnum.FAILED, launch.getStatus());

		verify(messageBus, times(1)).publishActivity(ArgumentMatchers.any());
	}

	@Test
	void changeStatusNegative() throws Exception {
		UpdateTestItemRQ request = new UpdateTestItemRQ();
		request.setStatus("failed");

		mockMvc.perform(put(SUPERADMIN_PROJECT_BASE_URL + "/item/5/update").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().is(400));
	}

	@Test
	void bulkUpdateItemAttributes() throws Exception {
		BulkInfoUpdateRQ request = new BulkInfoUpdateRQ();
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);
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

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/info").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		List<TestItem> items = testItemRepository.findAllById(launchIds);
		items.forEach(it -> testItemRepository.refresh(it));

		items.forEach(it -> {
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
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);
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

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/info").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		List<TestItem> items = testItemRepository.findAllById(launchIds);
		items.forEach(it -> testItemRepository.refresh(it));

		items.forEach(it -> {
			assertTrue(it.getAttributes()
					.stream()
					.anyMatch(attr -> "createdKey".equals(attr.getKey()) && attr.getValue().equals("createdValue") && !attr.isSystem()));
			assertTrue(it.getDescription().length() > comment.length() && it.getDescription().contains(comment));
		});
	}

	@Test
	void bulkDeleteAttributes() throws Exception {
		BulkInfoUpdateRQ request = new BulkInfoUpdateRQ();
		List<Long> launchIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L);
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

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/item/info").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());

		List<TestItem> items = testItemRepository.findAllById(launchIds);
		items.forEach(it -> testItemRepository.refresh(it));

		items.forEach(it -> {
			assertTrue(it.getAttributes()
					.stream()
					.noneMatch(attr -> "testKey".equals(attr.getKey()) && attr.getValue().equals("testValue") && !attr.isSystem()));
			assertEquals(comment, it.getDescription());
		});
	}
}