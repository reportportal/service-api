/*
 * Copyright 2025 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.file.upload.OwnedUploadFile;
import com.epam.reportportal.base.core.file.upload.OwnedUploadStore;
import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.log.LogService;
import com.epam.reportportal.base.infrastructure.persistence.binary.AttachmentBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.AttachmentMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.service.LogTypeResolver;
import com.epam.reportportal.base.reporting.SaveLogRQ;
import jakarta.servlet.MultipartConfigElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * The synchronous reporting API saves the log and its attachment after the response. These tests
 * run the deferred work only after the "request" has ended and its part file is gone, as the
 * servlet container does.
 */
@ExtendWith(MockitoExtension.class)
class CreateLogHandlerImplTest {

  private static final String LAUNCH_UUID = "5a2b1c3d-0000-4000-8000-000000000001";

  @Mock
  TestItemRepository testItemRepository;
  @Mock
  TestItemService testItemService;
  @Mock
  LaunchRepository launchRepository;
  @Mock
  LogRepository logRepository;
  @Mock
  AttachmentBinaryDataService attachmentBinaryDataService;
  @Mock
  LogService logService;
  @Mock
  LogTypeResolver logTypeResolver;

  @InjectMocks
  CreateLogHandlerImpl handler;

  @TempDir
  Path spool;

  private OwnedUploadStore store;
  private final List<Runnable> deferred = new ArrayList<>();

  @BeforeEach
  void setUp() {
    store = new OwnedUploadStore(new MultipartConfigElement(spool.toString()));
    ReflectionTestUtils.setField(handler, "ownedUploadStore", store);
    ReflectionTestUtils.setField(handler, "taskExecutor", (TaskExecutor) deferred::add);

    Launch launch = new Launch();
    launch.setId(10L);
    launch.setUuid(LAUNCH_UUID);
    launch.setProjectId(1L);
    lenient().when(testItemRepository.findByUuid(any())).thenReturn(Optional.empty());
    lenient().when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(launch));
    lenient().when(logTypeResolver.resolveLogLevelFromName(anyLong(), anyString())).thenReturn(40000);
    lenient().when(logRepository.saveAndFlush(any(Log.class))).thenAnswer(inv -> {
      Log log = inv.getArgument(0);
      log.setId(77L);
      return log;
    });
  }

  @Test
  void attachmentAboveThresholdIsSavedAfterTheRequestEnded() throws IOException {
    byte[] content = new byte[3 * 1024 * 1024];
    new java.util.Random(1).nextBytes(content);
    SpooledPart part = new SpooledPart(spool, "file", "big.bin", "application/zip", content);
    AtomicReference<byte[]> saved = new AtomicReference<>();
    ArgumentCaptor<MultipartFile> file = ArgumentCaptor.forClass(MultipartFile.class);
    doAnswer(inv -> {
      try (InputStream in = inv.<MultipartFile>getArgument(0).getInputStream()) {
        saved.set(in.readAllBytes());
      }
      return null;
    }).when(attachmentBinaryDataService)
        .saveFileAndAttachToLog(file.capture(), any(AttachmentMetaInfo.class));

    var rs = handler.createLog(request(), part, membership());
    part.endRequest();
    runDeferred();

    assertArrayEquals(content, saved.get(), "attachment content must survive the request");
    MultipartFile passed = file.getValue();
    assertInstanceOf(OwnedUploadFile.class, passed);
    assertEquals("file", passed.getName());
    assertEquals("big.bin", passed.getOriginalFilename());
    assertEquals("application/zip", passed.getContentType());
    assertEquals(content.length, passed.getSize());
    ArgumentCaptor<AttachmentMetaInfo> meta = ArgumentCaptor.forClass(AttachmentMetaInfo.class);
    verify(attachmentBinaryDataService).saveFileAndAttachToLog(any(), meta.capture());
    assertEquals(77L, meta.getValue().getLogId());
    assertEquals(rs.getId(), meta.getValue().getLogUuid());
    assertEquals("big.bin", meta.getValue().getFileName());
    assertNoOwnedFiles();
  }

  @Test
  void failingAttachmentSaveReleasesTheOwnedFile() throws IOException {
    SpooledPart part = new SpooledPart(spool, "file", "a.txt", "text/plain", new byte[]{1, 2, 3});
    doThrow(new IllegalStateException("data store down")).when(attachmentBinaryDataService)
        .saveFileAndAttachToLog(any(), any());

    handler.createLog(request(), part, membership());
    part.endRequest();
    assertEquals(1, ownedFiles().size(), "owned until the deferred work has run");
    runDeferred();

    assertNoOwnedFiles();
  }

  @Test
  void failingLogSaveReleasesTheOwnedFile() throws IOException {
    SpooledPart part = new SpooledPart(spool, "file", "a.txt", "text/plain", new byte[]{1});
    when(logRepository.saveAndFlush(any(Log.class))).thenThrow(new IllegalStateException("fk"));

    handler.createLog(request(), part, membership());
    part.endRequest();
    runDeferred();

    verify(attachmentBinaryDataService, never()).saveFileAndAttachToLog(any(), any());
    assertNoOwnedFiles();
  }

  @Test
  void rejectedTaskReleasesTheOwnedFileAndFailsTheRequest() throws IOException {
    SpooledPart part = new SpooledPart(spool, "file", "a.txt", "text/plain", new byte[]{1});
    ReflectionTestUtils.setField(handler, "taskExecutor", (TaskExecutor) r -> {
      throw new TaskRejectedException("full");
    });

    assertThrows(TaskRejectedException.class,
        () -> handler.createLog(request(), part, membership()));

    assertNoOwnedFiles();
  }

  @Test
  void logWithoutAttachmentCreatesNoOwnedFile() throws IOException {
    handler.createLog(request(), null, membership());
    runDeferred();

    verify(logService).saveLogMessage(any(), any());
    verify(attachmentBinaryDataService, never()).saveFileAndAttachToLog(any(), any());
    assertNoOwnedFiles();
  }

  private void runDeferred() {
    // stages may enqueue further stages
    while (!deferred.isEmpty()) {
      deferred.removeFirst().run();
    }
  }

  private static SaveLogRQ request() {
    SaveLogRQ rq = new SaveLogRQ();
    rq.setLaunchUuid(LAUNCH_UUID);
    rq.setLevel("ERROR");
    rq.setMessage("msg");
    rq.setLogTime(Instant.now());
    return rq;
  }

  private static MembershipDetails membership() {
    return MembershipDetails.builder().withProjectId(1L).build();
  }

  private List<Path> ownedFiles() throws IOException {
    try (Stream<Path> files = Files.list(store.getDirectory())) {
      return files.toList();
    }
  }

  private void assertNoOwnedFiles() throws IOException {
    assertEquals(List.of(), ownedFiles(), "owned upload files must be released");
  }

  /**
   * A part spooled to disk the way the servlet container does it: readable until the request ends,
   * then deleted; {@code transferTo(File)} renames it.
   */
  static final class SpooledPart implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final Path file;

    SpooledPart(Path dir, String name, String originalFilename, String contentType, byte[] content)
        throws IOException {
      this.name = name;
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.file = Files.write(Files.createTempFile(dir, "upload_", ".tmp"), content);
    }

    void endRequest() throws IOException {
      Files.deleteIfExists(file);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getOriginalFilename() {
      return originalFilename;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public boolean isEmpty() {
      return getSize() == 0;
    }

    @Override
    public long getSize() {
      return file.toFile().length();
    }

    @Override
    public byte[] getBytes() throws IOException {
      return Files.readAllBytes(file);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return Files.newInputStream(file);
    }

    @Override
    public void transferTo(File dest) throws IOException {
      Files.move(file, dest.toPath());
    }
  }
}
