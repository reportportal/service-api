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

package com.epam.ta.reportportal.core.log.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.MembershipUtils.rpUserToMembership;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ReportingQueueService;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import javax.inject.Provider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Konstantin Antipin
 */

@ExtendWith(MockitoExtension.class)
class CreateLogHandlerAsyncImplTest {

  @Mock
  Provider<SaveLogBinaryDataTaskAsync> provider;

  @Mock
  ReportingQueueService reportingQueueService;

  @Mock
  AmqpTemplate amqpTemplate;

  @Mock
  TaskExecutor taskExecutor;

  @InjectMocks
  CreateLogHandlerAsyncImpl createLogHandlerAsync;

  @Mock
  MultipartFile multipartFile;

  @Mock
  SaveLogBinaryDataTaskAsync saveLogBinaryDataTask;

  @Mock
  BinaryDataMetaInfo binaryDataMetaInfo;

  @Test
  void createLog() {
    SaveLogRQ request = new SaveLogRQ();
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.EDITOR,
        1L);

    when(provider.get()).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withRequest(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withFile(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withProjectId(any())).thenReturn(saveLogBinaryDataTask);

    createLogHandlerAsync.createLog(request, multipartFile,
        rpUserToMembership(user));

    verify(provider).get();
    verify(saveLogBinaryDataTask).withRequest(request);
    verify(saveLogBinaryDataTask).withFile(multipartFile);
    verify(saveLogBinaryDataTask).withProjectId(
        rpUserToMembership(user).getProjectId());
  }

  @Test
  void sendMessage() {
    SaveLogRQ request = new SaveLogRQ();

    createLogHandlerAsync.sendMessage(request, binaryDataMetaInfo, 0L);
    verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
    verify(reportingQueueService).getReportingQueueKey(any());
  }

}
