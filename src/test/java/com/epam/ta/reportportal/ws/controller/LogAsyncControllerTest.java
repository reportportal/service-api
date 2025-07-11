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

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.reporting.async.controller.LogAsyncController;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Konstantin Antipin
 */
@ExtendWith(MockitoExtension.class)
class LogAsyncControllerTest {

  @Mock
  ProjectExtractor projectExtractor;

  @Mock
  CreateLogHandler createLogHandler;

  @Mock
  Validator validator;

  @InjectMocks
  LogAsyncController logAsyncController;

  @Mock
  HttpServletRequest httpServletRequest;

  @Test
  void createLog() {
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    SaveLogRQ saveLogRQ = new SaveLogRQ();

    ArgumentCaptor<SaveLogRQ> requestArgumentCaptor = ArgumentCaptor.forClass(SaveLogRQ.class);
    ArgumentCaptor<MultipartFile> fileArgumentCaptor = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<MembershipDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(
        MembershipDetails.class);

    when(projectExtractor.extractMembershipDetails(any(ReportPortalUser.class),
        anyString())).thenReturn(rpUserToMembership(user));

    logAsyncController.createLog(TEST_PROJECT_KEY, saveLogRQ, user);
    verify(createLogHandler).createLog(requestArgumentCaptor.capture(),
        fileArgumentCaptor.capture(), projectDetailsArgumentCaptor.capture());
    verify(validator).validate(requestArgumentCaptor.capture());

    requestArgumentCaptor.getAllValues().forEach(rq -> assertEquals(saveLogRQ, rq));
    assertNull(fileArgumentCaptor.getValue());
    assertEquals(rpUserToMembership(user), projectDetailsArgumentCaptor.getValue());
  }

  @Test
  void createLogEntry() {
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    SaveLogRQ saveLogRQ = new SaveLogRQ();

    ArgumentCaptor<SaveLogRQ> requestArgumentCaptor = ArgumentCaptor.forClass(SaveLogRQ.class);
    ArgumentCaptor<MultipartFile> fileArgumentCaptor = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<MembershipDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(
        MembershipDetails.class);

    when(projectExtractor.extractMembershipDetails(any(ReportPortalUser.class),
        anyString())).thenReturn(rpUserToMembership(user));

    logAsyncController.createLogEntry(TEST_PROJECT_KEY, saveLogRQ, user);
    verify(createLogHandler).createLog(requestArgumentCaptor.capture(),
        fileArgumentCaptor.capture(), projectDetailsArgumentCaptor.capture());
    verify(validator).validate(requestArgumentCaptor.capture());

    requestArgumentCaptor.getAllValues().forEach(rq -> assertEquals(saveLogRQ, rq));
    assertNull(fileArgumentCaptor.getValue());
    assertEquals(rpUserToMembership(user),
        projectDetailsArgumentCaptor.getValue());
  }

  @Test
  void createLogs() {
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    SaveLogRQ saveLogRQ = new SaveLogRQ();
    SaveLogRQ[] saveLogRQs = {saveLogRQ, saveLogRQ};

    ArgumentCaptor<SaveLogRQ> requestArgumentCaptor = ArgumentCaptor.forClass(SaveLogRQ.class);
    ArgumentCaptor<MultipartFile> fileArgumentCaptor = ArgumentCaptor.forClass(MultipartFile.class);
    ArgumentCaptor<MembershipDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(
        MembershipDetails.class);

    when(projectExtractor.extractMembershipDetails(any(ReportPortalUser.class),
        anyString())).thenReturn(rpUserToMembership(user));

    logAsyncController.createLog(TEST_PROJECT_KEY, saveLogRQs, httpServletRequest, user);
    verify(validator, times(4)).validate(requestArgumentCaptor.capture());
    verify(createLogHandler, times(2)).createLog(requestArgumentCaptor.capture(),
        fileArgumentCaptor.capture(), projectDetailsArgumentCaptor.capture());

    assertEquals(6, requestArgumentCaptor.getAllValues().size());
    assertEquals(2, fileArgumentCaptor.getAllValues().size());
    assertEquals(2, projectDetailsArgumentCaptor.getAllValues().size());

    requestArgumentCaptor.getAllValues().forEach(arg -> assertEquals(saveLogRQ, arg));
    fileArgumentCaptor.getAllValues().forEach(Assertions::assertNull);
    projectDetailsArgumentCaptor.getAllValues()
        .forEach(arg -> assertEquals(rpUserToMembership(user), arg));
  }
}
