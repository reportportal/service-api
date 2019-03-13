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

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.config.ProjectConfigurationUpdate;
import com.epam.ta.reportportal.ws.model.project.email.LaunchAttribute;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/project/project-fill.sql")
@ExtendWith(MockitoExtension.class)
class ProjectControllerTest extends BaseMvcTest {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private Client rabbitClient;

	@Autowired
	@Qualifier("analyzerRabbitTemplate")
	private RabbitTemplate rabbitTemplate;

	@AfterEach
	void after() {
		Mockito.reset(rabbitClient, rabbitTemplate);
	}

	@Test
	void createProjectPositive() throws Exception {
		CreateProjectRQ rq = new CreateProjectRQ();
		rq.setProjectName("TestProject");
		rq.setEntryType("INTERNAL");
		mockMvc.perform(post("/project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isCreated());
		final Optional<Project> createdProjectOptional = projectRepository.findByName("TestProject".toLowerCase());
		assertTrue(createdProjectOptional.isPresent());
		assertEquals(12, createdProjectOptional.get().getProjectAttributes().size());
		assertEquals(5, createdProjectOptional.get().getProjectIssueTypes().size());
	}

	@Test
	void updateProjectPositive() throws Exception {
		final UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("job.keepLogs", "2 weeks");
		projectAttributes.put("job.interruptJobTime", "1 week");
		projectAttributes.put("job.keepScreenshots", "3 weeks");
		projectAttributes.put("analyzer.autoAnalyzerMode", "CURRENT_LAUNCH");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteProjectPositive() throws Exception {
		mockMvc.perform(delete("/project/test_project").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void bulkDeleteProjects() throws Exception {
		BulkRQ<DeleteProjectRQ> bulkRQ = new BulkRQ<>();
		Map<Long, DeleteProjectRQ> entities = new HashMap<>();
		DeleteProjectRQ first = new DeleteProjectRQ();
		first.setProjectName("default_personal");
		entities.put(2L, first);
		DeleteProjectRQ second = new DeleteProjectRQ();
		second.setProjectName("test_project");
		entities.put(3L, second);
		bulkRQ.setEntities(entities);
		mockMvc.perform(delete("/project").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(bulkRQ))).andExpect(status().isOk());
	}

	@Test
	void getProjectUsersPositive() throws Exception {
		mockMvc.perform(get("/project/test_project/users").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectPositive() throws Exception {
		mockMvc.perform(get("/project/test_project").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void unassignProjectUsersPositive() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(singletonList("test_user"));
		mockMvc.perform(put("/project/test_project/unassign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void unassignUsersEmptyUserNames() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(new ArrayList<>());
		mockMvc.perform(put("/project/test_project/unassign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isBadRequest());
	}

	@Test
	void assignProjectUsersPositive() throws Exception {
		AssignUsersRQ rq = new AssignUsersRQ();
		Map<String, String> user = new HashMap<>();
		user.put("default", "MEMBER");
		rq.setUserNames(user);
		mockMvc.perform(put("/project/test_project/assign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void getUsersForAssignPositive() throws Exception {
		mockMvc.perform(get("/project/test_project/assignable").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectUsersTest() throws Exception {
		mockMvc.perform(get("/project/test_project/usernames?filter.cnt.users=user").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void addUserPreference() throws Exception {
		mockMvc.perform(put("/project/test_project/preference/superadmin/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getUserPreferencePositive() throws Exception {
		mockMvc.perform(get("/project/test_project/preference/superadmin").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void deleteUserPreference() throws Exception {
		mockMvc.perform(delete("/project/test_project/preference/superadmin/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAllProjectNames() throws Exception {
		mockMvc.perform(get("/project/names").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Sql("/db/test-item/test-item-fill.sql")
	@Test
	void getProjectInfoPositive() throws Exception {
		mockMvc.perform(get("/project/list/default_personal").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.projectId").value(2))
				.andExpect(jsonPath("$.projectName").value("default_personal"))
				.andExpect(jsonPath("$.usersQuantity").value(1))
				.andExpect(jsonPath("$.launchesQuantity").value(1))
				.andExpect(jsonPath("$.entryType").value("PERSONAL"));
	}

	@Sql("/db/test-item/test-item-fill.sql")
	@Test
	void getProjectInfoWithoutLaunches() throws Exception {
		mockMvc.perform(get("/project/list/superadmin_personal").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.projectId").value(1))
				.andExpect(jsonPath("$.projectName").value("superadmin_personal"))
				.andExpect(jsonPath("$.usersQuantity").value(1))
				.andExpect(jsonPath("$.launchesQuantity").value(0))
				.andExpect(jsonPath("$.entryType").value("PERSONAL"));
	}

	@Sql("/db/test-item/test-item-fill.sql")
	@Test
	void getAllProjectInfo() throws Exception {
		mockMvc.perform(get("/project/list").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));
	}

	@Test
	void searchForUsername() throws Exception {
		mockMvc.perform(get("/project/test_project/usernames/search?term=user").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void export() throws Exception {
		final ResultActions resultActions = mockMvc.perform(get("/project/export").with(token(oAuthHelper.getSuperadminToken())));
		resultActions.andExpect(status().isOk());
		assertEquals("text/csv", resultActions.andReturn().getResponse().getContentType());
	}

	@Test
	void getInvestigatedProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/investigated").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.toInvestigate").value("66.67"))
				.andExpect(jsonPath("$..values.investigated").value("33.33"));
	}

	@Test
	void getCasesStatsProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/casesStats").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.min").value("5.0"))
				.andExpect(jsonPath("$..values.avg").value("5"))
				.andExpect(jsonPath("$..values.max").value("5.0"));
	}

	@Test
	void getLaunchesQuantityProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/launchesQuantity").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.count").value("1"));
	}

	@Test
	void getIssuesChartProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/issuesChart").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.systemIssue").value("0"))
				.andExpect(jsonPath("$..values.automationBug").value("0"))
				.andExpect(jsonPath("$..values.toInvestigate").value("2"))
				.andExpect(jsonPath("$..values.productBug").value("1"));
	}

	@Test
	void getBugPercentageProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/bugsPercentage").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getActivitiesProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/activities").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getLastLaunchProjectWidget() throws Exception {
		mockMvc.perform(get("/project/test_project/widget/lastLaunch").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").isNotEmpty())
				.andExpect(jsonPath("$.result.id").value(2))
				.andExpect(jsonPath("$.result.name").value("test launch"))
				.andExpect(jsonPath("$.result.statistics").isNotEmpty());
	}

	@Test
	void getAnalyzerIndexingStatus() throws Exception {
		mockMvc.perform(get("/project/analyzer/status").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.superadmin_personal").value(false))
				.andExpect(jsonPath("$.default_personal").value(false))
				.andExpect(jsonPath("$.test_project").value(false));

	}

	@Test
	void updateProjectNotificationConfig() throws Exception {
		ProjectNotificationConfigDTO request = new ProjectNotificationConfigDTO();

		SenderCaseDTO senderCaseDTO = new SenderCaseDTO();
		senderCaseDTO.setSendCase("always");
		senderCaseDTO.setRecipients(Collections.singletonList("default"));
		senderCaseDTO.setLaunchNames(Collections.singletonList("test launch"));
		LaunchAttribute launchAttribute = new LaunchAttribute();
		launchAttribute.setKey("key");
		launchAttribute.setValue("val");
		senderCaseDTO.setAttributes(Sets.newHashSet(launchAttribute));

		request.setSenderCases(singletonList(senderCaseDTO));

		mockMvc.perform(put("/project/default_personal/notification").with(token(oAuthHelper.getDefaultToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(request))).andExpect(status().isOk());
	}

	@Test
	void indexProjectData() throws Exception {
		ExchangeInfo exchangeInfo = new ExchangeInfo();
		exchangeInfo.setName("analyzer");
		HashMap<String, Object> arguments = new HashMap<>();
		arguments.put("analyzer_index", true);
		arguments.put("analyzer", "test_analyzer");
		exchangeInfo.setArguments(arguments);
		when(rabbitClient.getExchanges(any())).thenReturn(Collections.singletonList(exchangeInfo));

		mockMvc.perform(put("/project/default_personal/index").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
	}

	@Test
	void deleteIndex() throws Exception {
		ExchangeInfo exchangeInfo = new ExchangeInfo();
		exchangeInfo.setName("analyzer");
		HashMap<String, Object> arguments = new HashMap<>();
		arguments.put("analyzer_index", true);
		arguments.put("analyzer", "test_analyzer");
		exchangeInfo.setArguments(arguments);
		when(rabbitClient.getExchanges(any())).thenReturn(Collections.singletonList(exchangeInfo));

		mockMvc.perform(delete("/project/default_personal/index").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		verify(rabbitTemplate, times(1)).convertAndSend(eq(exchangeInfo.getName()), eq("delete"), eq(2L));
	}
}