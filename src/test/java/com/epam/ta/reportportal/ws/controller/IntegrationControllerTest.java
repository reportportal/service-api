package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;
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
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("email");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(post("/integration").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isConflict());
	}

	@Test
	void createGlobalIntegrationNegative() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("unknown");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(post("/integration").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void createProjectIntegration() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("email");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(post("/integration" + DEFAULT_PROJECT_BASE_URL).with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isConflict());
	}

	@Test
	void createProjectIntegrationNegative() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("unknown");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(post("/integration" + DEFAULT_PROJECT_BASE_URL).with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void updateGlobalIntegration() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("email");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(put("/integration/8").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void updateGlobalIntegrationNegative() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("unknown");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(put("/integration/8").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void updateProjectIntegration() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("email");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		doNothing().when(emailService).testConnection();

		mockMvc.perform(put("/integration" + DEFAULT_PROJECT_BASE_URL + "/8").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void updateProjectIntegrationNegative() throws Exception {
		UpdateIntegrationRQ request = new UpdateIntegrationRQ();
		request.setIntegrationName("unknown");
		Map<String, Object> params = new HashMap<>();
		params.put("param1", "value");
		params.put("param2", "lalala");
		request.setIntegrationParams(params);
		request.setEnabled(true);

		mockMvc.perform(put("/integration" + DEFAULT_PROJECT_BASE_URL + "/8").with(token(oAuthHelper.getDefaultToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isNotFound());
	}

	@Test
	void getGlobalIntegration() throws Exception {
		mockMvc.perform(get("/integration/7").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getGlobalIntegrationNegative() throws Exception {
		mockMvc.perform(get("/integration/100").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isNotFound());
	}

	@Test
	void deleteGlobalIntegration() throws Exception {
		mockMvc.perform(delete("/integration/7").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteGlobalIntegrationNegative() throws Exception {
		mockMvc.perform(delete("/integration/100").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isNotFound());
	}

	@Test
	void deleteAllIntegrations() throws Exception {
		mockMvc.perform(delete("/integration/all").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectIntegration() throws Exception {
		mockMvc.perform(get("/integration/default_personal/8").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectIntegrationNegative() throws Exception {
		mockMvc.perform(get("/integration/default_personal/100").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteProjectIntegration() throws Exception {
		mockMvc.perform(delete("/integration/default_personal/8").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteProjectIntegrationNegative() throws Exception {
		mockMvc.perform(delete("/integration/default_personal/100").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteAllProjectIntegrations() throws Exception {
		mockMvc.perform(delete("/integration/default_personal/all").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}
}