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

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.MergeLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.launch.util.LinkGenerator;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.reporting.async.controller.LaunchAsyncController;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author Konstantin Antipin
 */
@ExtendWith(MockitoExtension.class)
class LaunchAsyncControllerTest {

  @Mock
  ProjectExtractor projectExtractor;

  @Mock
  StartLaunchHandler startLaunchHandler;

  @Mock
  FinishLaunchHandler finishLaunchHandler;

  @Mock
  MergeLaunchHandler mergeLaunchHandler;

  @InjectMocks
  LaunchAsyncController launchAsyncController;

  @Test
  void startLaunch() {
    ReportPortalUser user =
        getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

    StartLaunchRQ startLaunchRQ = new StartLaunchRQ();

    ArgumentCaptor<ReportPortalUser> userArgumentCaptor =
        ArgumentCaptor.forClass(ReportPortalUser.class);
    ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor =
        ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
    ArgumentCaptor<StartLaunchRQ> requestArgumentCaptor =
        ArgumentCaptor.forClass(StartLaunchRQ.class);

    when(projectExtractor.extractProjectDetails(any(ReportPortalUser.class),
        anyString()
    )).thenReturn(user.getProjectDetails().get("test_project"));

    launchAsyncController.startLaunch("test_project", startLaunchRQ, user);
    verify(startLaunchHandler).startLaunch(userArgumentCaptor.capture(),
        projectDetailsArgumentCaptor.capture(), requestArgumentCaptor.capture()
    );
    assertEquals(user, userArgumentCaptor.getValue());
    assertEquals(user.getProjectDetails().get("test_project"),
        projectDetailsArgumentCaptor.getValue()
    );
    assertEquals(startLaunchRQ, requestArgumentCaptor.getValue());
  }

  @Test
  void finishLaunch() {
    ReportPortalUser user =
        getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();

    String launchId = UUID.randomUUID().toString();

    ArgumentCaptor<String> launchIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<FinishExecutionRQ> requestArgumentCaptor =
        ArgumentCaptor.forClass(FinishExecutionRQ.class);
    ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor =
        ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
    ArgumentCaptor<ReportPortalUser> userArgumentCaptor =
        ArgumentCaptor.forClass(ReportPortalUser.class);
    ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);

    when(projectExtractor.extractProjectDetails(any(ReportPortalUser.class),
        anyString()
    )).thenReturn(user.getProjectDetails().get("test_project"));

    MockedStatic<LinkGenerator> a = mockStatic(LinkGenerator.class);
    a.when(() -> LinkGenerator.composeBaseUrl(any()))
        .thenReturn("http://localhost:8080/api");

    launchAsyncController.finishLaunch("test_project", launchId, finishExecutionRQ, user,null);
    verify(finishLaunchHandler).finishLaunch(launchIdArgumentCaptor.capture(),
        requestArgumentCaptor.capture(), projectDetailsArgumentCaptor.capture(),
        userArgumentCaptor.capture(), urlArgumentCaptor.capture()
    );
    assertEquals(user, userArgumentCaptor.getValue());
    assertEquals(user.getProjectDetails().get("test_project"),
        projectDetailsArgumentCaptor.getValue()
    );
    assertEquals(finishExecutionRQ, requestArgumentCaptor.getValue());
  }

  @Test
  void mergeLaunch() {
    ReportPortalUser user =
        getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

    MergeLaunchesRQ mergeLaunchesRQ = new MergeLaunchesRQ();

    ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor =
        ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
    ArgumentCaptor<ReportPortalUser> userArgumentCaptor =
        ArgumentCaptor.forClass(ReportPortalUser.class);
    ArgumentCaptor<MergeLaunchesRQ> requestArgumentCaptor =
        ArgumentCaptor.forClass(MergeLaunchesRQ.class);

    when(projectExtractor.extractProjectDetails(any(ReportPortalUser.class),
        anyString()
    )).thenReturn(user.getProjectDetails().get("test_project"));

    launchAsyncController.mergeLaunches("test_project", mergeLaunchesRQ, user);
    verify(mergeLaunchHandler).mergeLaunches(projectDetailsArgumentCaptor.capture(),
        userArgumentCaptor.capture(), requestArgumentCaptor.capture()
    );
    assertEquals(user, userArgumentCaptor.getValue());
    assertEquals(user.getProjectDetails().get("test_project"),
        projectDetailsArgumentCaptor.getValue()
    );
    assertEquals(mergeLaunchesRQ, requestArgumentCaptor.getValue());
  }
}
