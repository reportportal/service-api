package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.Position;
import com.epam.ta.reportportal.ws.model.Size;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/shareable/shareable-fill.sql")
class DashboardControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Test
	void createDashboardPositive() throws Exception {
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("dashboard");
		createDashboardRQ.setDescription("description");
		final MvcResult mvcResult = mockMvc.perform(post(DEFAULT_PROJECT_BASE_URL + "/dashboard").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(createDashboardRQ))
				.contentType(APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		final EntryCreatedRS entryCreatedRS = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), EntryCreatedRS.class);
		final Optional<Dashboard> dashboardOptional = dashboardRepository.findById(entryCreatedRS.getId());
		assertTrue(dashboardOptional.isPresent());
		assertEquals("dashboard", dashboardOptional.get().getName());
		assertEquals("description", dashboardOptional.get().getDescription());
	}

	@Test
	void getAllDashboardsPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/dashboard").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void getDashboardPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/dashboard/17").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void updateDashboardPositive() throws Exception {
		final UpdateDashboardRQ rq = new UpdateDashboardRQ();
		rq.setName("updated");
		rq.setDescription("updated");
		rq.setShare(false);
		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/dashboard/17").with(token(oAuthHelper.getDefaultToken()))
				.content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		final Optional<Dashboard> optionalDashboard = dashboardRepository.findById(17L);
		assertTrue(optionalDashboard.isPresent());
		assertEquals("updated", optionalDashboard.get().getName());
		assertEquals("updated", optionalDashboard.get().getDescription());
		assertFalse(optionalDashboard.get().isShared());
	}

	@Test
	void deleteDashboardPositive() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/dashboard/17").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getSharedDashboardsNamesPositive() throws Exception {
		mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/dashboard/shared").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void removeWidget() throws Exception {
		mockMvc.perform(delete(DEFAULT_PROJECT_BASE_URL + "/dashboard/18/10").with(token(oAuthHelper.getDefaultToken())))
				.andExpect(status().isOk());
	}

	@Test
	void addWidget() throws Exception {
		AddWidgetRq rq = new AddWidgetRq();
		rq.setAddWidget(new DashboardResource.WidgetObjectModel("kek", 10L, new Size(5, 5), new Position(0, 0)));

		mockMvc.perform(put(DEFAULT_PROJECT_BASE_URL + "/dashboard/17/add").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}
}