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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.MergeLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.savedrequest.Enumerator;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Konstantin Antipin
 */
@ExtendWith(MockitoExtension.class)
class LaunchAsyncControllerTest {

    @Mock
    StartLaunchHandler startLaunchHandler;

    @Mock
    FinishLaunchHandler finishLaunchHandler;

    @Mock
    MergeLaunchHandler mergeLaunchHandler;

    @InjectMocks
    LaunchAsyncController launchAsyncController;

    @Mock
    HttpServletRequest httpServletRequest;

    @Test
    void startLaunch() {
        ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

        StartLaunchRQ startLaunchRQ = new StartLaunchRQ();

        ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
        ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
        ArgumentCaptor<StartLaunchRQ> requestArgumentCaptor = ArgumentCaptor.forClass(StartLaunchRQ.class);

        launchAsyncController.startLaunch("test_project", startLaunchRQ, user);
        verify(startLaunchHandler).startLaunch(userArgumentCaptor.capture(), projectDetailsArgumentCaptor.capture(), requestArgumentCaptor.capture());
        assertEquals(user, userArgumentCaptor.getValue());
        assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
        assertEquals(startLaunchRQ, requestArgumentCaptor.getValue());
    }

    @Test
    void finishLaunch() {
        ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

        FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();

        String launchId = UUID.randomUUID().toString();

        ArgumentCaptor<String> launchIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<FinishExecutionRQ> requestArgumentCaptor = ArgumentCaptor.forClass(FinishExecutionRQ.class);
        ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
        ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
        ArgumentCaptor<String> urlArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));
        when(httpServletRequest.getHeaderNames()).thenReturn(new Enumerator<>(Lists.newArrayList()));
        launchAsyncController.finishLaunch("test_project", launchId, finishExecutionRQ, user, httpServletRequest);
        verify(finishLaunchHandler).finishLaunch(
                launchIdArgumentCaptor.capture(),
                requestArgumentCaptor.capture(),
                projectDetailsArgumentCaptor.capture(),
                userArgumentCaptor.capture(),
                urlArgumentCaptor.capture());
        assertEquals(user, userArgumentCaptor.getValue());
        assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
        assertEquals(finishExecutionRQ, requestArgumentCaptor.getValue());
    }

    @Test
    void mergeLaunch() {
        ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

        MergeLaunchesRQ mergeLaunchesRQ = new MergeLaunchesRQ();

        ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
        ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
        ArgumentCaptor<MergeLaunchesRQ> requestArgumentCaptor = ArgumentCaptor.forClass(MergeLaunchesRQ.class);

        launchAsyncController.mergeLaunches("test_project", mergeLaunchesRQ, user);
        verify(mergeLaunchHandler).mergeLaunches(projectDetailsArgumentCaptor.capture(), userArgumentCaptor.capture(), requestArgumentCaptor.capture());
        assertEquals(user, userArgumentCaptor.getValue());
        assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
        assertEquals(mergeLaunchesRQ, requestArgumentCaptor.getValue());
    }
}