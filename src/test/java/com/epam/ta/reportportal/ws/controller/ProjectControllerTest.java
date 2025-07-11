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

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.model.project.config.ProjectConfigurationUpdate;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.ExchangeInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

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
  @Disabled("waiting for requirements")
  void createProjectPositive() throws Exception {
		CreateProjectRQ rq = new CreateProjectRQ();
		rq.setProjectName("TestProject");
    rq.setOrganizationId(1L);

    mockMvc.perform(post("/v1/project")
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isCreated());
		final Optional<Project> createdProjectOptional = projectRepository.findByName("TestProject".toLowerCase());
		assertTrue(createdProjectOptional.isPresent());
		assertEquals(15, createdProjectOptional.get().getProjectAttributes().size());
    assertEquals(5, createdProjectOptional.get().getProjectIssueTypes().size());
  }

  @Test
  @Disabled("waiting for requirements")
  void createProjectWithReservedName() throws Exception {
    CreateProjectRQ rq = new CreateProjectRQ();
    rq.setProjectName("project");
    mockMvc.perform(post("/v1/project").content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
  }

  @Test
  void updateProjectPositive() throws Exception {

    ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
    HashMap<String, String> projectAttributes = new HashMap<>();
    projectAttributes.put("notifications.enabled", "false");
    //2 weeks in seconds
    projectAttributes.put("job.keepLaunches", String.valueOf(3600 * 24 * 14));
    //2 weeks in seconds
    projectAttributes.put("job.keepLogs", String.valueOf(3600 * 24 * 14));
    //1 week in seconds
    projectAttributes.put("job.interruptJobTime", String.valueOf(3600 * 24 * 7));
    //1 week in seconds
    projectAttributes.put("job.keepScreenshots", String.valueOf(3600 * 24 * 7));
    projectAttributes.put("analyzer.autoAnalyzerMode", "CURRENT_LAUNCH");
    projectAttributes.put("analyzer.minShouldMatch", "5");
    projectAttributes.put("analyzer.numberOfLogLines", "5");
    projectAttributes.put("analyzer.isAutoAnalyzerEnabled", "false");
    projectAttributes.put("analyzer.searchLogsMinShouldMatch", "60");
    projectAttributes.put("analyzer.uniqueError.enabled", "true");
    projectAttributes.put("analyzer.uniqueError.removeNumbers", "true");
    configuration.setProjectAttributes(projectAttributes);
		final UpdateProjectRQ rq = new UpdateProjectRQ();
    rq.setConfiguration(configuration);

    HashMap<String, String> userRoles = new HashMap<>();
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isOk());

    Project project = projectRepository.findByKey("test_project")
        .orElseThrow(() -> new AssertionError("Test project 'test_project' not found"));
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
  void updateProjectNegativeWithWrongBounds() throws Exception {
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
		final UpdateProjectRQ rq = new UpdateProjectRQ();
    rq.setConfiguration(configuration);

    HashMap<String, String> userRoles = new HashMap<>();
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
  }

  @Test
  void updateProjectConfigKeepLogsNegativeTest() throws Exception {
    ProjectConfigurationUpdate configuration = new ProjectConfigurationUpdate();
    HashMap<String, String> projectAttributes = new HashMap<>();
    projectAttributes.put("notifications.enabled", "false");
    projectAttributes.put("job.keepLogs", "110000d");
    configuration.setProjectAttributes(projectAttributes);
		UpdateProjectRQ rq = new UpdateProjectRQ();
    rq.setConfiguration(configuration);

    HashMap<String, String> userRoles = new HashMap<>();
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
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
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
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
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
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
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
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
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
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
    userRoles.put("test_user", "EDITOR");
    rq.setUserRoles(userRoles);
    mockMvc.perform(put("/v1/project/test_project")
        .content(objectMapper.writeValueAsBytes(rq))
        .contentType(APPLICATION_JSON)
        .with(token(oAuthHelper.getSuperadminToken()))).andExpect(status().isBadRequest());
  }

  @Test
  void deleteProject() throws Exception {
    assertTrue(projectRepository.findById(3L).isPresent());

    mockMvc.perform(delete("/organizations/101/projects/3")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());

    assertFalse(projectRepository.findById(3L).isPresent());
  }

  @Test
  void deleteProjectWrongOrganization() throws Exception {
    assertTrue(projectRepository.findById(3L).isPresent());

    mockMvc.perform(delete("/organizations/1/projects/3")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());

    assertTrue(projectRepository.findById(3L).isPresent());
  }

  @Test
  @Disabled("waiting for requirements")
  void bulkDeleteProjects() throws Exception {
    mockMvc.perform(delete("/v1/project")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .param("ids", "2", "3"))
        .andExpect(status().isOk());
  }

  @Test
  void getProjectUsersPositive() throws Exception {
    mockMvc.perform(
            get("/v1/project/test_project/users")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getProjectPositive() throws Exception {
    mockMvc.perform(get("/v1/project/test_project")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void unassignProjectUsersPositive() throws Exception {
    UnassignUsersRQ rq = new UnassignUsersRQ();
    rq.setUsernames(singletonList("test_user"));
    mockMvc.perform(
        put("/v1/project/test_project/unassign")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
  }

  @Test
  void unassignUsersEmptyUserNames() throws Exception {
    UnassignUsersRQ rq = new UnassignUsersRQ();
    rq.setUsernames(new ArrayList<>());
    mockMvc.perform(
        put("/v1/project/test_project/unassign")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isBadRequest());
  }

  @Test
  void assignProjectUsersPositive() throws Exception {
    AssignUsersRQ rq = new AssignUsersRQ();
    Map<String, String> user = new HashMap<>();
    user.put("default@reportportal.internal", "EDITOR");
    rq.setUserNames(user);
    mockMvc.perform(
        put("/v1/project/test_project/assign")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(rq))).andExpect(status().isOk());
  }

  @Test
  void getUsersForAssignPositive() throws Exception {
    mockMvc.perform(
            get("/v1/project/test_project/assignable")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getProjectUsersTest() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/usernames?filter.cnt.users=user")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void addUserPreference() throws Exception {
    mockMvc.perform(
            put("/v1/project/test_project/preference/2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void addUserPreferenceNegative() throws Exception {
    mockMvc.perform(
            put("/v1/project/test_project/preference/2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
    mockMvc.perform(
            put("/v1/project/test_project/preference/2")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isConflict());
  }

  @Test
  void getUserPreferencePositive() throws Exception {
    mockMvc.perform(
            get("/v1/project/test_project/preference")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void deleteUserPreference() throws Exception {
    mockMvc.perform(delete("/v1/project/test_project/preference/1").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getAllProjectNames() throws Exception {
    mockMvc.perform(get("/v1/project/names")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void searchProjectNames() throws Exception {
    mockMvc.perform(
            get("/v1/project/names/search?term=UpEr")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Sql("/db/test-item/test-item-fill.sql")
  @Test
  void getProjectInfoPositive() throws Exception {
    mockMvc.perform(
            get("/v1/project/list/default_personal")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.projectName").value("default_personal"))
        .andExpect(jsonPath("$.usersQuantity").value(1))
        .andExpect(jsonPath("$.launchesQuantity").value(1));
  }

  @Sql("/db/test-item/test-item-fill.sql")
  @Test
  void getProjectInfoWithoutLaunches() throws Exception {
    mockMvc.perform(
            get("/v1/project/list/superadmin_personal")
                .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.projectName").value("superadmin_personal"))
        .andExpect(jsonPath("$.usersQuantity").value(1))
        .andExpect(jsonPath("$.launchesQuantity").value(0));
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
    mockMvc.perform(get("/v1/project/test_project/usernames/search?term=user").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void export() throws Exception {
    final ResultActions resultActions = mockMvc.perform(
        get("/v1/project/export").with(token(oAuthHelper.getSuperadminToken())));
    resultActions.andExpect(status().isOk());
    assertEquals("text/csv", resultActions.andReturn().getResponse().getContentType());
  }

  @Test
  void getInvestigatedProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/investigated").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..values.toInvestigate").value("66.67"))
        .andExpect(jsonPath("$..values.investigated").value("33.33"));
  }

  @Test
  void getCasesStatsProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/casesStats").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..values.min").value("5.0"))
        .andExpect(jsonPath("$..values.avg").value("5"))
        .andExpect(jsonPath("$..values.max").value("5.0"));
  }

  @Test
  void getLaunchesQuantityProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/launchesQuantity").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..values.count").value("1"));
  }

  @Test
  void getIssuesChartProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/issuesChart").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..values.systemIssue").value("0"))
        .andExpect(jsonPath("$..values.automationBug").value("0"))
        .andExpect(jsonPath("$..values.toInvestigate").value("2"))
        .andExpect(jsonPath("$..values.productBug").value("1"));
  }

  @Test
  void getBugPercentageProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/bugsPercentage").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getActivitiesProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/activities").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void getLastLaunchProjectWidget() throws Exception {
    mockMvc.perform(get("/v1/project/test_project/widget/lastLaunch").with(
            token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").isNotEmpty())
        .andExpect(jsonPath("$.result.id").value(2))
        .andExpect(jsonPath("$.result.name").value("test launch"))
        .andExpect(jsonPath("$.result.statistics").isNotEmpty());
  }

  @Test
  void getAnalyzerIndexingStatus() throws Exception {
    mockMvc.perform(
            get("/v1/project/analyzer/status")
                .with(token(oAuthHelper.getSuperadminToken())))
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
    senderCaseDTO.setRecipients(Collections.singletonList("default@example.com"));
    senderCaseDTO.setLaunchNames(Collections.singletonList("test launch"));
    senderCaseDTO.setEnabled(true);
    senderCaseDTO.setRuleName("rule #1");
    ItemAttributeResource launchAttribute = new ItemAttributeResource();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    senderCaseDTO.setAttributes(Sets.newHashSet(launchAttribute));
    senderCaseDTO.setAttributesOperator(LogicalOperator.AND.getOperator());

    request.setSenderCases(singletonList(senderCaseDTO));

    mockMvc.perform(
        put("/v1/project/default_personal/notification")
            .with(token(oAuthHelper.getDefaultToken()))
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
    when(rabbitClient.getExchanges(any(String.class))).thenReturn(
        Collections.singletonList(exchangeInfo));

    mockMvc.perform(
            put("/v1/project/default_personal/index")
                .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());

    verifyProjectIndexEvent();
  }

  @Test
  void deleteIndex() throws Exception {
    ExchangeInfo exchangeInfo = new ExchangeInfo();
    exchangeInfo.setName("analyzer");
    HashMap<String, Object> arguments = new HashMap<>();
    arguments.put("analyzer_index", true);
    arguments.put("analyzer", "test_analyzer");
    exchangeInfo.setArguments(arguments);
    when(rabbitClient.getExchanges(any(String.class))).thenReturn(
        Collections.singletonList(exchangeInfo));

    mockMvc.perform(
            delete("/v1/project/default_personal/index")
                .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk());

    verifyProjectIndexEvent();

    verify(rabbitTemplate, times(1))
        .convertSendAndReceiveAsType(eq(exchangeInfo.getName()),
        eq("delete"), eq(2L), any());
  }

  private void verifyProjectIndexEvent() {
    final ArgumentCaptor<ProjectIndexEvent> eventArgumentCaptor = ArgumentCaptor.forClass(
        ProjectIndexEvent.class);
    verify(messageBus, times(1))
        .publishActivity(eventArgumentCaptor.capture());

    final ProjectIndexEvent event = eventArgumentCaptor.getValue();
    assertEquals(2L, event.getProjectId().longValue());
    assertEquals("default_personal", event.getProjectName());
    assertEquals(2L, event.getUserId().longValue());
    assertEquals("default@reportportal.internal", event.getUserLogin());
  }
}
