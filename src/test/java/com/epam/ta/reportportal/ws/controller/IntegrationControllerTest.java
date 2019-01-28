/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql(value = { "classpath:db/integration/integration-type-fill.sql", "classpath:db/integration/integrations-fill.sql" })
public class IntegrationControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper testObjectMapper;

	@Test
	public void createGlobalIntegration() throws Exception {

		UpdateIntegrationRQ updateIntegrationRQ = new UpdateIntegrationRQ();

		updateIntegrationRQ.setIntegrationName("EMAIL");
		updateIntegrationRQ.setEnabled(true);
		Map<String, Object> params = new HashMap<>();
		params.put("from", "test@mail.com");
		updateIntegrationRQ.setIntegrationParams(params);

		mockMvc.perform(MockMvcRequestBuilders.post("/integration")
				.with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(testObjectMapper.writeValueAsString(updateIntegrationRQ))).andExpect(status().isCreated());
	}

	@Test
	public void createProjectIntegration() throws Exception {

		UpdateIntegrationRQ updateIntegrationRQ = new UpdateIntegrationRQ();

		updateIntegrationRQ.setIntegrationName("EMAIL");
		updateIntegrationRQ.setEnabled(true);
		Map<String, Object> params = new HashMap<>();
		params.put("from", "test@mail.com");
		updateIntegrationRQ.setIntegrationParams(params);

		mockMvc.perform(MockMvcRequestBuilders.post("/integration" + SUPERADMIN_PROJECT_BASE_URL)
				.with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(testObjectMapper.writeValueAsString(updateIntegrationRQ))).andExpect(status().isCreated());
	}

	@Test
	public void getIntegration() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/integration/7").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteGlobalIntegration() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.delete("/integration/7").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void deleteAllIntegrations() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.delete("/integration/all").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	public void getProjectIntegration() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/integration" + SUPERADMIN_PROJECT_BASE_URL + "/9")
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void deleteProjectIntegration() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.delete("/integration" + SUPERADMIN_PROJECT_BASE_URL + "/9")
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	public void deleteAllProjectIntegrations() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.delete("/integration" + SUPERADMIN_PROJECT_BASE_URL + "/all")
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}
}