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

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.reporting.async.producer.LogProducer;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import java.util.UUID;
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
  AmqpTemplate amqpTemplate;

  @InjectMocks
  LogProducer createLogHandlerAsync;

  @Mock
  MultipartFile multipartFile;

  @Mock
  SaveLogBinaryDataTaskAsync saveLogBinaryDataTask;

  @Mock
  BinaryDataMetaInfo binaryDataMetaInfo;

  @Mock
  TaskExecutor saveLogsTaskExecutor;

  @Test
  void createLog() {
    SaveLogRQ request = new SaveLogRQ();
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER,
        1L);

    when(provider.get()).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withRequest(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withFile(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withProjectId(any())).thenReturn(saveLogBinaryDataTask);

    createLogHandlerAsync.createLog(request, multipartFile,
        user.getProjectDetails().get("test_project"));

    verify(provider).get();
    verify(saveLogBinaryDataTask).withRequest(request);
    verify(saveLogBinaryDataTask).withFile(multipartFile);
    verify(saveLogBinaryDataTask).withProjectId(
        user.getProjectDetails().get("test_project").getProjectId());
  }

  @Test
  void sendMessage() {
    SaveLogRQ request = new SaveLogRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());

    createLogHandlerAsync.sendMessage(request, binaryDataMetaInfo, 0L);
    verify(amqpTemplate).convertAndSend(any(), any(), any(), any());
  }


  @Test
  void sendMessageWithoutLaunchUuid() {
    SaveLogRQ request = new SaveLogRQ();

    ReportPortalException exception = assertThrows(
        ReportPortalException.class,
        () -> createLogHandlerAsync.sendMessage(request, binaryDataMetaInfo, 0L)
    );
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'Launch UUID should not be null or empty.'",
        exception.getMessage()
    );
  }

}