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

package com.epam.reportportal.base.core.log.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.file.upload.OwnedUploadFile;
import com.epam.reportportal.base.core.file.upload.OwnedUploadStore;
import com.epam.reportportal.base.infrastructure.persistence.commons.BinaryDataMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.SaveLogRQ;
import com.epam.reportportal.base.reporting.async.producer.LogProducer;
import jakarta.inject.Provider;
import jakarta.servlet.MultipartConfigElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Konstantin Antipin
 */

@ExtendWith(MockitoExtension.class)
class CreateLogHandlerAsyncImplTest {

  @Mock
  Provider<SaveLogBinaryDataTaskAsync> provider;

  @Mock
  RabbitTemplate amqpTemplate;

  @InjectMocks
  LogProducer createLogHandlerAsync;

  MultipartFile multipartFile = new MockMultipartFile("file", "a.txt", "text/plain",
      new byte[]{1, 2, 3});

  @TempDir
  Path spool;

  OwnedUploadStore store;

  @Mock
  SaveLogBinaryDataTaskAsync saveLogBinaryDataTask;

  @Mock
  BinaryDataMetaInfo binaryDataMetaInfo;

  @Mock
  TaskExecutor saveLogsTaskExecutor;

  @BeforeEach
  void setUp() {
    store = new OwnedUploadStore(new MultipartConfigElement(spool.toString()));
    ReflectionTestUtils.setField(createLogHandlerAsync, "ownedUploadStore", store);
  }

  @Test
  void createLog() throws IOException {
    SaveLogRQ request = new SaveLogRQ();
    ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.EDITOR,
        1L);

    when(provider.get()).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withRequest(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withFile(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withProjectId(any())).thenReturn(saveLogBinaryDataTask);

    createLogHandlerAsync.createLog(request, multipartFile,
        rpUserToMembership(user));

    verify(provider).get();
    verify(saveLogBinaryDataTask).withRequest(request);
    ArgumentCaptor<MultipartFile> passed = ArgumentCaptor.forClass(MultipartFile.class);
    verify(saveLogBinaryDataTask).withFile(passed.capture());
    assertInstanceOf(OwnedUploadFile.class, passed.getValue(),
        "the task runs after the request: it must get a copy the service owns");
    assertEquals("a.txt", passed.getValue().getOriginalFilename());
    assertArrayEquals(new byte[]{1, 2, 3}, passed.getValue().getBytes());
    verify(saveLogBinaryDataTask).withProjectId(
        rpUserToMembership(user).getProjectId());
  }

  @Test
  void ownedFileIsReleasedAfterTheBinaryIsSavedAndTheMessageSent() throws IOException {
    SaveLogRQ request = new SaveLogRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());
    List<Runnable> deferred = new ArrayList<>();
    ReflectionTestUtils.setField(createLogHandlerAsync, "taskExecutor", (TaskExecutor) deferred::add);
    stubTask();
    when(saveLogBinaryDataTask.get()).thenReturn(binaryDataMetaInfo);

    createLogHandlerAsync.createLog(request, multipartFile, membership());
    assertEquals(1, ownedFiles().size());
    deferred.forEach(Runnable::run);

    verify(amqpTemplate).invoke(any());
    assertEquals(List.of(), ownedFiles());
  }

  @Test
  void ownedFileIsReleasedWhenTheBinarySaveFails() throws IOException {
    SaveLogRQ request = new SaveLogRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());
    List<Runnable> deferred = new ArrayList<>();
    ReflectionTestUtils.setField(createLogHandlerAsync, "taskExecutor", (TaskExecutor) deferred::add);
    stubTask();
    when(saveLogBinaryDataTask.get()).thenThrow(new IllegalStateException("data store down"));

    createLogHandlerAsync.createLog(request, multipartFile, membership());
    deferred.forEach(Runnable::run);

    verify(amqpTemplate, never()).invoke(any());
    assertEquals(List.of(), ownedFiles());
  }

  @Test
  void ownedFileIsReleasedWhenTheTaskIsRejected() throws IOException {
    ReflectionTestUtils.setField(createLogHandlerAsync, "taskExecutor", (TaskExecutor) r -> {
      throw new TaskRejectedException("full");
    });
    stubTask();

    assertThrows(TaskRejectedException.class,
        () -> createLogHandlerAsync.createLog(new SaveLogRQ(), multipartFile, membership()));

    assertEquals(List.of(), ownedFiles());
  }

  private void stubTask() {
    when(provider.get()).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withRequest(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withFile(any())).thenReturn(saveLogBinaryDataTask);
    when(saveLogBinaryDataTask.withProjectId(any())).thenReturn(saveLogBinaryDataTask);
  }

  private static MembershipDetails membership() {
    return MembershipDetails.builder().withProjectId(1L).build();
  }

  private List<Path> ownedFiles() throws IOException {
    try (var files = Files.list(store.getDirectory())) {
      return files.toList();
    }
  }

  @Test
  void sendMessage() {
    SaveLogRQ request = new SaveLogRQ();
    request.setLaunchUuid(UUID.randomUUID().toString());

    createLogHandlerAsync.sendMessage(request, binaryDataMetaInfo, 0L);
    verify(amqpTemplate).invoke(any());
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
