/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.ReportPortalUserUtil.TEST_PROJECT_NAME;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ImportFinishedEvent;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.util.ProjectExtractor;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tests for {@link ImportPluginCommandHandlerImpl}.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@ExtendWith(MockitoExtension.class)
class ImportPluginCommandHandlerImplTest {

  private static final String PLUGIN_NAME = "test-plugin";
  private static final String LAUNCH_UUID = "launch-uuid";

  @Mock
  private ExecuteIntegrationHandler executeIntegrationHandler;

  @Mock
  private ProjectExtractor projectExtractor;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private MessageBus messageBus;

  @Mock
  private MultipartFile file;

  @InjectMocks
  private ImportPluginCommandHandlerImpl handler;

  @Test
  void shouldRejectMemberImportToAnotherUsersLaunch() {
    shouldRejectImportToAnotherUsersLaunch(ProjectRole.MEMBER);
  }

  @Test
  void shouldRejectCustomerImportToAnotherUsersLaunch() {
    shouldRejectImportToAnotherUsersLaunch(ProjectRole.CUSTOMER);
  }

  @Test
  void shouldExecuteImportForOwnLaunch() {
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, 1L);
    ReportPortalUser.ProjectDetails projectDetails = projectDetails(ProjectRole.MEMBER);
    LaunchImportRQ rq = importRequest();
    Launch launch = launch();
    Object result = new Object();

    when(projectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME)).thenReturn(
        projectDetails);
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(launch));
    when(file.getOriginalFilename()).thenReturn("launch.zip");
    when(executeIntegrationHandler.executeCommand(eq(projectDetails), eq(PLUGIN_NAME),
        eq("import"), argThat((Map<String, Object> params) ->
            params.get("file") == file && params.get(ENTITY_PARAM) == rq)
    )).thenReturn(result);

    Object actual = handler.execute(user, TEST_PROJECT_NAME, PLUGIN_NAME, file, rq);

    assertSame(result, actual);

    ArgumentCaptor<ImportFinishedEvent> eventCaptor =
        ArgumentCaptor.forClass(ImportFinishedEvent.class);
    verify(messageBus).publishActivity(eventCaptor.capture());
    assertEquals(1L, eventCaptor.getValue().getProjectId());
    assertEquals("launch.zip", eventCaptor.getValue().getFileName());
  }

  @Test
  void shouldExecuteImportWithoutTargetLaunchValidationWhenLaunchUuidIsNotSpecified() {
    ReportPortalUser user = getRpUser("member", UserRole.USER, ProjectRole.MEMBER, 1L);
    ReportPortalUser.ProjectDetails projectDetails = projectDetails(ProjectRole.MEMBER);
    Object result = new Object();

    when(projectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME)).thenReturn(
        projectDetails);
    when(executeIntegrationHandler.executeCommand(eq(projectDetails), eq(PLUGIN_NAME),
        eq("import"), any()
    )).thenReturn(result);

    Object actual = handler.execute(user, TEST_PROJECT_NAME, PLUGIN_NAME, file, null);

    assertSame(result, actual);
    verifyNoInteractions(launchRepository);
  }

  private void shouldRejectImportToAnotherUsersLaunch(ProjectRole projectRole) {
    ReportPortalUser user = getRpUser("user", UserRole.USER, projectRole, 1L);
    ReportPortalUser.ProjectDetails projectDetails = projectDetails(projectRole);
    LaunchImportRQ rq = importRequest();
    Launch launch = launch();
    launch.setUserId(2L);

    when(projectExtractor.extractProjectDetails(user, TEST_PROJECT_NAME)).thenReturn(
        projectDetails);
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(launch));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.execute(user, TEST_PROJECT_NAME, PLUGIN_NAME, file, rq));

    assertEquals(ACCESS_DENIED, exception.getErrorType());
    assertEquals("You do not have enough permissions. You are not launch owner.",
        exception.getMessage());
    verify(executeIntegrationHandler, never()).executeCommand(
        any(ReportPortalUser.ProjectDetails.class), anyString(), anyString(), anyMap());
    verify(messageBus, never()).publishActivity(any());
  }

  private static ReportPortalUser.ProjectDetails projectDetails(ProjectRole projectRole) {
    return new ReportPortalUser.ProjectDetails(1L, TEST_PROJECT_NAME, projectRole);
  }

  private static LaunchImportRQ importRequest() {
    LaunchImportRQ rq = new LaunchImportRQ();
    rq.setLaunchUuid(LAUNCH_UUID);
    return rq;
  }

  private static Launch launch() {
    Launch launch = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).orElseThrow();
    launch.setUuid(LAUNCH_UUID);
    return launch;
  }
}
