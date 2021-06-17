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
import com.epam.ta.reportportal.ws.model.integration.IntegrationRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/integration/integration-fill.sql")
class IntegrationControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void createGlobalIntegration() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		request.setName("email");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(post("/v1/integration/email").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/integration/email").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isConflict());
	}

	@Test
	void createGlobalIntegrationNegative() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		request.setName("name");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(post("/v1/integration/unknown").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void createProjectIntegration() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		request.setName("email");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(post("/v1/integration/default_personal/email").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/integration/default_personal/email").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isConflict());
	}

	@Test
	void createProjectIntegrationNegative() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(post("/v1/integration/default_personal/unknown").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void updateGlobalIntegration() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(put("/v1/integration/7").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void updateGlobalIntegrationNegative() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(put("/v1/integration/77").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void updateProjectIntegration() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(put("/v1/integration/default_personal/8").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void updateProjectIntegrationNegative() throws Exception {
		IntegrationRQ request = new IntegrationRQ();
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(put("/v1/integration/default_personal/88").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void getAllGlobal() throws Exception {
		mockMvc.perform(get("/v1/integration/global/all").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getAllGlobalByType() throws Exception {
		mockMvc.perform(get("/v1/integration/global/all/jira").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getAllProject() throws Exception {
		mockMvc.perform(get("/v1/integration/project/superadmin_personal/all").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAllProjectByType() throws Exception {
		mockMvc.perform(get("/v1/integration/project/superadmin_personal/all/jira").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getGlobalIntegration() throws Exception {
		mockMvc.perform(get("/v1/integration/7").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}


	@Test
	void getGlobalIntegrationNegative() throws Exception {
		mockMvc.perform(get("/v1/integration/100").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isNotFound());
	}

	@Test
	void deleteGlobalIntegration() throws Exception {
		mockMvc.perform(delete("/v1/integration/7").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteGlobalIntegrationNegative() throws Exception {
		mockMvc.perform(delete("/v1/integration/100").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isNotFound());
	}

	@Test
	void deleteAllIntegrations() throws Exception {
		mockMvc.perform(delete("/v1/integration/all/email").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectIntegration() throws Exception {
		mockMvc.perform(get("/v1/integration/default_personal/8").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void testProjectIntegrationConnection() throws Exception {
		mockMvc.perform(get("/v1/integration/default_personal/8/connection/test").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectIntegrationNegative() throws Exception {
		mockMvc.perform(get("/v1/integration/default_personal/100").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteProjectIntegration() throws Exception {
		mockMvc.perform(delete("/v1/integration/default_personal/8").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteProjectIntegrationNegative() throws Exception {
		mockMvc.perform(delete("/v1/integration/default_personal/100").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteAllProjectIntegrations() throws Exception {
		mockMvc.perform(delete("/v1/integration/default_personal/all/email").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}
}