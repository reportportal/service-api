package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class SettingsControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void getServerSettings() throws Exception {
		mockMvc.perform(get("/settings").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void updateAnalyticsSettings() throws Exception {
		AnalyticsResource resource = new AnalyticsResource();
		resource.setType("server.analytics.all");
		resource.setEnabled(true);
		mockMvc.perform(put("/settings/analytics").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resource))).andExpect(status().isOk());
	}

	@Test
	void saveAnalyticsSettingsNegative() throws Exception {
		AnalyticsResource resource = new AnalyticsResource();
		resource.setEnabled(true);
		resource.setType("");
		mockMvc.perform(put("/settings/analytics").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(resource))).andExpect(status().isBadRequest());
	}
}