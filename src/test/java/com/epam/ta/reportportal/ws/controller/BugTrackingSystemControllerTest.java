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

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/integration/bts-integration-fill.sql")
class BugTrackingSystemControllerTest extends BaseMvcTest {

	public static final String TICKET_ID = "/ticket_id";

	private BtsExtension extension = mock(BtsExtension.class);

	@MockBean
	private Pf4jPluginBox pluginBox;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void updateGlobalBtsIntegration() throws Exception {

		UpdateBugTrackingSystemRQ request = getUpdateRQ();

		mockMvc.perform(put("/bts" + "/9").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void updateProjectBtsIntegration() throws Exception {

		UpdateBugTrackingSystemRQ request = getUpdateRQ();

		mockMvc.perform(put("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void checkConnection() throws Exception {

		when(pluginBox.getInstance("JIRA", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.testConnection(any(Integration.class))).thenReturn(true);

		mockMvc.perform(put("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/connect").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getConnectionRQ()))).andExpect(status().isOk());
	}

	@Test
	void getSetOfIntegrationSystemFields() throws Exception {

		Map<String, List<String>> params = Maps.newHashMap();
		params.put("issueType", Lists.newArrayList("ISSUE01"));

		when(pluginBox.getInstance("JIRA", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.getTicketFields(any(String.class), any(Integration.class))).thenReturn(Lists.newArrayList(new PostFormField()));

		mockMvc.perform(get("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/fields-set").params(CollectionUtils.toMultiValueMap(params))
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getAllowableIssueTypes() throws Exception {

		when(pluginBox.getInstance("JIRA", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.getIssueTypes(any(Integration.class))).thenReturn(Lists.newArrayList("type1", "type2"));

		mockMvc.perform(get("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/issue_types").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void createIssue() throws Exception {

		PostTicketRQ request = getPostTicketRQ();

		when(pluginBox.getInstance("JIRA", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.submitTicket(any(PostTicketRQ.class), any(Integration.class))).thenReturn(new Ticket());

		mockMvc.perform(post("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/ticket").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isCreated());
	}

	@Test
	void getTicket() throws Exception {

		Map<String, List<String>> params = Maps.newHashMap();
		params.put("url", Lists.newArrayList("jira.com"));
		params.put("btsProject", Lists.newArrayList("project"));

		when(pluginBox.getInstance("JIRA", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.getTicket(any(String.class), any(Integration.class))).thenReturn(java.util.Optional.of(new Ticket()));

		mockMvc.perform(get("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/ticket" + TICKET_ID).params(CollectionUtils.toMultiValueMap(params))
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	private UpdateBugTrackingSystemRQ getUpdateRQ() {

		UpdateBugTrackingSystemRQ updateBugTrackingSystemRQ = new UpdateBugTrackingSystemRQ();

		updateBugTrackingSystemRQ.setFields(getPostFormFields());

		return updateBugTrackingSystemRQ;
	}

	private BtsConnectionTestRQ getConnectionRQ() {
		BtsConnectionTestRQ connectionTestRQ = new BtsConnectionTestRQ();
		connectionTestRQ.setUrl("url");
		connectionTestRQ.setBtsProject("project");

		return connectionTestRQ;
	}

	private PostTicketRQ getPostTicketRQ() {
		PostTicketRQ postTicketRQ = new PostTicketRQ();
		postTicketRQ.setFields(getPostFormFields());
		postTicketRQ.setNumberOfLogs(10);
		postTicketRQ.setIsIncludeScreenshots(false);
		postTicketRQ.setIsIncludeComments(false);
		postTicketRQ.setTestItemId(1L);

		return postTicketRQ;
	}

	private List<PostFormField> getPostFormFields() {

		PostFormField field = new PostFormField("id",
				"name",
				"type",
				true,
				Lists.newArrayList("value"),
				Lists.newArrayList(new AllowedValue("id", "name"))
		);

		return Lists.newArrayList(field);
	}
}