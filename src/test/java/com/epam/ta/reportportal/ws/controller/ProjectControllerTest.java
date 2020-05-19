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
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.model.DeleteBulkRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.config.ProjectConfigurationUpdate;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
import static org.junit.jupiter.api.Assertions.*;
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
		mockMvc.perform(post("/v1/project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isCreated());
		final Optional<Project> createdProjectOptional = projectRepository.findByName("TestProject".toLowerCase());
		assertTrue(createdProjectOptional.isPresent());
		assertEquals(11, createdProjectOptional.get().getProjectAttributes().size());
		assertEquals(5, createdProjectOptional.get().getProjectIssueTypes().size());
	}

	@Test
	void createProjectWithReservedName() throws Exception {
		CreateProjectRQ rq = new CreateProjectRQ();
		rq.setProjectName("project");
		rq.setEntryType("INTERNAL");
		mockMvc.perform(post("/v1/project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void updateProjectPositive() throws Exception {
		final UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("notifications.enabled", "false");
		//2 weeks in seconds
		projectAttributes.put("job.keepLaunches", String.valueOf(3600 * 24 * 14));
		//2 weeks in seconds
		projectAttributes.put("job.keepLogs", String.valueOf(3600 * 24 * 14));
		//1 week in seconds
		projectAttributes.put("job.interruptJobTime", String.valueOf(3600 * 24 * 7));
		//3 weeks in seconds
		projectAttributes.put("job.keepScreenshots", String.valueOf(3600 * 24 * 21));
		projectAttributes.put("analyzer.autoAnalyzerMode", "CURRENT_LAUNCH");
		projectAttributes.put("analyzer.minShouldMatch", "5");
		projectAttributes.put("analyzer.numberOfLogLines", "5");
		projectAttributes.put("analyzer.isAutoAnalyzerEnabled", "false");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());

		Project project = projectRepository.findByName("test_project").get();
		projectAttributes.forEach((key, value) -> {
			Optional<ProjectAttribute> pa = project.getProjectAttributes()
					.stream()
					.filter(it -> it.getAttribute().getName().equalsIgnoreCase(key))
					.findAny();
			assertTrue(pa.isPresent());
			assertEquals(value, pa.get().getValue());
		});
	}

	@Test
	void updateProjectConfigKeepLogsNegativeTest() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("notifications.enabled", "false");
		projectAttributes.put("job.keepLogs", "110000d");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void updateProjectConfigKeepLaunchesNegativeTest() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("notifications.enabled", "false");
		projectAttributes.put("job.keepLaunches", "110000f");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void updateProjectConfigKeepScreenshotsNegativeTest() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("notifications.enabled", "false");
		projectAttributes.put("job.keepScreenshots", "123123.2");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void updateProjectConfigInterruptLaunchesNegativeTest() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("notifications.enabled", "false");
		projectAttributes.put("job.interruptJobTime", "incorrect");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void updateProjectConfigurationIncorrectAttributeTest() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("incorrect", "false");
		projectAttributes.put("job.keepLogs", "2 weeks");
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void updateProjectConfigurationNullValueTest() throws Exception {
		UpdateProjectRQ rq = new UpdateProjectRQ();
		ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
		HashMap<String, String> projectAttributes = new HashMap<>();
		projectAttributes.put("job.keepLogs", null);
		configuration.setProjectAttributes(projectAttributes);
		rq.setConfiguration(configuration);

		HashMap<String, String> userRoles = new HashMap<>();
		userRoles.put("test_user", "PROJECT_MANAGER");
		rq.setUserRoles(userRoles);
		mockMvc.perform(put("/v1/project/test_project").content(objectMapper.writeValueAsBytes(rq))
				.contentType(APPLICATION_JSON)
				.with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
	}

	@Test
	void deleteProjectPositive() throws Exception {
		mockMvc.perform(delete("/v1/project/3").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());

		assertFalse(projectRepository.findById(3L).isPresent());
	}

	@Test
	void bulkDeleteProjects() throws Exception {
		DeleteBulkRQ bulkRQ = new DeleteBulkRQ();
		bulkRQ.setIds(Lists.newArrayList(2L, 3L));
		mockMvc.perform(delete("/v1/project").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(bulkRQ))).andExpect(status().isOk());
	}

	@Test
	void getProjectUsersPositive() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/users").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void getProjectPositive() throws Exception {
		mockMvc.perform(get("/v1/project/test_project").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void unassignProjectUsersPositive() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(singletonList("test_user"));
		mockMvc.perform(put("/v1/project/test_project/unassign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void unassignUsersEmptyUserNames() throws Exception {
		UnassignUsersRQ rq = new UnassignUsersRQ();
		rq.setUsernames(new ArrayList<>());
		mockMvc.perform(put("/v1/project/test_project/unassign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isBadRequest());
	}

	@Test
	void assignProjectUsersPositive() throws Exception {
		AssignUsersRQ rq = new AssignUsersRQ();
		Map<String, String> user = new HashMap<>();
		user.put("default", "MEMBER");
		rq.setUserNames(user);
		mockMvc.perform(put("/v1/project/test_project/assign").with(token(oAuthHelper.getSuperadminToken()))
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
	}

	@Test
	void getUsersForAssignPositive() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/assignable").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getProjectUsersTest() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/usernames?filter.cnt.users=user").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void addUserPreference() throws Exception {
		mockMvc.perform(put("/v1/project/test_project/preference/superadmin/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void addUserPreferenceNegative() throws Exception {
		mockMvc.perform(put("/v1/project/test_project/preference/superadmin/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
		mockMvc.perform(put("/v1/project/test_project/preference/superadmin/2").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isConflict());
	}

	@Test
	void getUserPreferencePositive() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/preference/superadmin").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void deleteUserPreference() throws Exception {
		mockMvc.perform(delete("/v1/project/test_project/preference/superadmin/1").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getAllProjectNames() throws Exception {
		mockMvc.perform(get("/v1/project/names").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Test
	void searchProjectNames() throws Exception {
		mockMvc.perform(get("/v1/project/names/search?term=UpEr").with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());
	}

	@Sql("/db/test-item/test-item-fill.sql")
	@Test
	void getProjectInfoPositive() throws Exception {
		mockMvc.perform(get("/v1/project/list/default_personal").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.projectName").value("default_personal"))
				.andExpect(jsonPath("$.usersQuantity").value(1))
				.andExpect(jsonPath("$.launchesQuantity").value(1))
				.andExpect(jsonPath("$.entryType").value("PERSONAL"));
	}

	@Sql("/db/test-item/test-item-fill.sql")
	@Test
	void getProjectInfoWithoutLaunches() throws Exception {
		mockMvc.perform(get("/v1/project/list/superadmin_personal").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.projectName").value("superadmin_personal"))
				.andExpect(jsonPath("$.usersQuantity").value(1))
				.andExpect(jsonPath("$.launchesQuantity").value(0))
				.andExpect(jsonPath("$.entryType").value("PERSONAL"));
	}

	@Sql("/db/test-item/test-item-fill.sql")
	@Test
	void getAllProjectInfo() throws Exception {
		mockMvc.perform(get("/v1/project/list").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));
	}

	@Test
	void searchForUsername() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/usernames/search?term=user").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void export() throws Exception {
		final ResultActions resultActions = mockMvc.perform(get("/v1/project/export").with(token(oAuthHelper.getSuperadminToken())));
		resultActions.andExpect(status().isOk());
		assertEquals("text/csv", resultActions.andReturn().getResponse().getContentType());
	}

	@Test
	void getInvestigatedProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/investigated").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.toInvestigate").value("66.67"))
				.andExpect(jsonPath("$..values.investigated").value("33.33"));
	}

	@Test
	void getCasesStatsProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/casesStats").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.min").value("5.0"))
				.andExpect(jsonPath("$..values.avg").value("5"))
				.andExpect(jsonPath("$..values.max").value("5.0"));
	}

	@Test
	void getLaunchesQuantityProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/launchesQuantity").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.count").value("1"));
	}

	@Test
	void getIssuesChartProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/issuesChart").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..values.systemIssue").value("0"))
				.andExpect(jsonPath("$..values.automationBug").value("0"))
				.andExpect(jsonPath("$..values.toInvestigate").value("2"))
				.andExpect(jsonPath("$..values.productBug").value("1"));
	}

	@Test
	void getBugPercentageProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/bugsPercentage").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getActivitiesProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/activities").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk());
	}

	@Test
	void getLastLaunchProjectWidget() throws Exception {
		mockMvc.perform(get("/v1/project/test_project/widget/lastLaunch").with(token(oAuthHelper.getSuperadminToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").isNotEmpty())
				.andExpect(jsonPath("$.result.id").value(2))
				.andExpect(jsonPath("$.result.name").value("test launch"))
				.andExpect(jsonPath("$.result.statistics").isNotEmpty());
	}

	@Test
	void getAnalyzerIndexingStatus() throws Exception {
		mockMvc.perform(get("/v1/project/analyzer/status").with(token(oAuthHelper.getSuperadminToken())))
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
		senderCaseDTO.setEnabled(true);
		ItemAttributeResource launchAttribute = new ItemAttributeResource();
		launchAttribute.setKey("key");
		launchAttribute.setValue("val");
		senderCaseDTO.setAttributes(Sets.newHashSet(launchAttribute));

		request.setSenderCases(singletonList(senderCaseDTO));

		mockMvc.perform(put("/v1/project/default_personal/notification").with(token(oAuthHelper.getDefaultToken()))
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

		mockMvc.perform(put("/v1/project/default_personal/index").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());
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

		mockMvc.perform(delete("/v1/project/default_personal/index").with(token(oAuthHelper.getDefaultToken()))).andExpect(status().isOk());

		verify(rabbitTemplate, times(1)).convertSendAndReceiveAsType(eq(exchangeInfo.getName()), eq("delete"), eq(2L), any());
	}
}