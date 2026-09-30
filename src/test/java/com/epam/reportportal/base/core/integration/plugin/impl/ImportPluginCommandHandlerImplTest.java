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

package com.epam.reportportal.base.core.integration.plugin.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.core.events.domain.ImportFinishedEvent;
import com.epam.reportportal.base.core.integration.ExecuteIntegrationHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.launch.LaunchImportRQ;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

/**
 * Tests for {@link ImportPluginCommandHandlerImpl}.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@ExtendWith(MockitoExtension.class)
class ImportPluginCommandHandlerImplTest {

  private static final String PROJECT_KEY = "o-slug.project-name";
  private static final String PLUGIN_NAME = "test-plugin";
  private static final String LAUNCH_UUID = "launch-uuid";

  @Mock
  private ExecuteIntegrationHandler executeIntegrationHandler;

  @Mock
  private ProjectExtractor projectExtractor;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Mock
  private MultipartFile file;

  @InjectMocks
  private ImportPluginCommandHandlerImpl handler;

  @Test
  void shouldRejectViewerImportToAnotherUsersLaunch() {
    shouldRejectImportToAnotherUsersLaunch();
  }

  @Test
  void shouldAllowEditorImportToAnotherUsersLaunch() {
    ReportPortalUser user = getRpUser("editor", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);
    MembershipDetails membershipDetails = membershipDetails(ProjectRole.EDITOR);
    LaunchImportRQ rq = importRequest();
    Launch launch = launch();
    launch.setUserId(2L);
    Object result = new Object();

    when(projectExtractor.extractMembershipDetails(user, PROJECT_KEY)).thenReturn(
        membershipDetails);
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(launch));
    when(file.getOriginalFilename()).thenReturn("launch.zip");
    when(executeIntegrationHandler.executeExtensionCommand(eq(PLUGIN_NAME), eq("import"),
        any())).thenReturn(result);

    Object actual = handler.execute(user, PROJECT_KEY, PLUGIN_NAME, file, rq);

    assertThat(actual).isSameAs(result);
  }

  @Test
  void shouldExecuteImportForOwnLaunch() {
    ReportPortalUser user = getRpUser("member", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);
    MembershipDetails membershipDetails = membershipDetails(ProjectRole.VIEWER);
    LaunchImportRQ rq = importRequest();
    Launch launch = launch();
    Object result = new Object();

    when(projectExtractor.extractMembershipDetails(user, PROJECT_KEY)).thenReturn(
        membershipDetails);
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(launch));
    when(file.getOriginalFilename()).thenReturn("launch.zip");
    when(executeIntegrationHandler.executeExtensionCommand(eq(PLUGIN_NAME), eq("import"),
        argThat((PluginCommandRQ pluginCommandRq) ->
            pluginCommandRq.getArguments().get("file") == file
                && pluginCommandRq.getArguments().get(ENTITY_PARAM) == rq)
    )).thenReturn(result);

    Object actual = handler.execute(user, PROJECT_KEY, PLUGIN_NAME, file, rq);

    assertThat(actual).isSameAs(result);

    ArgumentCaptor<ImportFinishedEvent> eventCaptor =
        ArgumentCaptor.forClass(ImportFinishedEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().getProjectId()).isEqualTo(1L);
    assertThat(eventCaptor.getValue().getFileName()).isEqualTo("launch.zip");
  }

  @Test
  void shouldExecuteImportWithoutTargetLaunchValidationWhenLaunchUuidIsNotSpecified() {
    ReportPortalUser user = getRpUser("member", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);
    MembershipDetails membershipDetails = membershipDetails(ProjectRole.VIEWER);
    Object result = new Object();

    when(projectExtractor.extractMembershipDetails(user, PROJECT_KEY)).thenReturn(
        membershipDetails);
    when(file.getOriginalFilename()).thenReturn("launch.zip");
    when(executeIntegrationHandler.executeExtensionCommand(eq(PLUGIN_NAME), eq("import"),
        any())).thenReturn(result);

    Object actual = handler.execute(user, PROJECT_KEY, PLUGIN_NAME, file, null);

    assertThat(actual).isSameAs(result);
    verifyNoInteractions(launchRepository);
  }

  private void shouldRejectImportToAnotherUsersLaunch() {
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);
    MembershipDetails membershipDetails = membershipDetails(ProjectRole.VIEWER);
    LaunchImportRQ rq = importRequest();
    Launch launch = launch();
    launch.setUserId(2L);

    when(projectExtractor.extractMembershipDetails(user, PROJECT_KEY)).thenReturn(
        membershipDetails);
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(launch));

    assertThatThrownBy(() -> handler.execute(user, PROJECT_KEY, PLUGIN_NAME, file, rq))
        .isInstanceOf(ReportPortalException.class)
        .satisfies(ex -> {
          ReportPortalException exception = (ReportPortalException) ex;
          assertThat(exception.getErrorType()).isEqualTo(ACCESS_DENIED);
          assertThat(exception.getMessage()).isEqualTo(
              "You do not have enough permissions. You are not launch owner.");
        });

    verify(executeIntegrationHandler, never()).executeExtensionCommand(
        anyString(), anyString(), any(PluginCommandRQ.class));
    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  private static MembershipDetails membershipDetails(ProjectRole projectRole) {
    return MembershipDetails.builder()
        .withOrgId(1L)
        .withProjectId(1L)
        .withProjectKey(PROJECT_KEY)
        .withProjectRole(projectRole)
        .build();
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
