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
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.externalsystem.*;
import com.epam.ta.reportportal.ws.model.integration.IntegrationRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/bts/bts-integration-fill.sql")
class BugTrackingSystemControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@Ignore
	void updateGlobalBtsIntegration() throws Exception {

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.testConnection(any(Integration.class))).thenReturn(true);

		IntegrationRQ request = getUpdateRQ();

		mockMvc.perform(put("/integration" + "/9").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	@Ignore
	void updateProjectBtsIntegration() throws Exception {

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.testConnection(any(Integration.class))).thenReturn(true);

		IntegrationRQ request = getUpdateRQ();

		mockMvc.perform(put("/integration" + SUPERADMIN_PROJECT_BASE_URL + "/10").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	@Ignore
	void checkConnection() throws Exception {

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.testConnection(any(Integration.class))).thenReturn(true);

		mockMvc.perform(get(SUPERADMIN_PROJECT_BASE_URL + "/integration/10/connection/test").with(token(oAuthHelper.getSuperadminToken())));
	}

	@Test
	void getSetOfIntegrationSystemFields() throws Exception {

		Map<String, List<String>> params = Maps.newHashMap();
		params.put("issueType", Lists.newArrayList("ISSUE01"));

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.getTicketFields(any(String.class), any(Integration.class))).thenReturn(Lists.newArrayList(new PostFormField()));

		mockMvc.perform(get("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/fields-set").params(CollectionUtils.toMultiValueMap(params))
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getAllowableIssueTypes() throws Exception {

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.getIssueTypes(any(Integration.class))).thenReturn(Lists.newArrayList("type1", "type2"));

		mockMvc.perform(get("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/issue_types").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void createIssue() throws Exception {

		PostTicketRQ request = getPostTicketRQ();

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.submitTicket(any(PostTicketRQ.class), any(Integration.class))).thenReturn(new Ticket());

		mockMvc.perform(post("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/10/ticket").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isCreated());
	}

	@Test
	void getTicket() throws Exception {

		final String ticketId = "/ticket_id";

		Map<String, List<String>> params = Maps.newHashMap();
		params.put("btsUrl", Lists.newArrayList("jira.com"));
		params.put("btsProject", Lists.newArrayList("project"));

		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(java.util.Optional.ofNullable(extension));
		when(extension.getTicket(any(String.class), any(Integration.class))).thenReturn(java.util.Optional.of(new Ticket()));

		mockMvc.perform(get("/bts" + SUPERADMIN_PROJECT_BASE_URL + "/ticket" + ticketId).params(CollectionUtils.toMultiValueMap(params))
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	private IntegrationRQ getUpdateRQ() {

		IntegrationRQ integrationRQ = new IntegrationRQ();
		integrationRQ.setEnabled(true);
		integrationRQ.setName("jira1");
		Map<String, Object> integrationParams = new HashMap<>();
		integrationParams.put("defectFormFields", getPostFormFields());
		integrationRQ.setIntegrationParams(integrationParams);
		return integrationRQ;
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