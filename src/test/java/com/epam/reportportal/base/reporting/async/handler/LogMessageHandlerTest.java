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

package com.epam.reportportal.base.reporting.async.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.log.LogService;
import com.epam.reportportal.base.infrastructure.persistence.binary.AttachmentBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.service.LogTypeResolver;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.config.RequestType;
import com.epam.reportportal.base.reporting.async.consumer.ReportingConsumer;
import com.epam.reportportal.base.reporting.async.exception.LogAlreadySavedException;
import com.epam.reportportal.base.reporting.async.handler.provider.ReportingHandlerProvider;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.dao.CannotAcquireLockException;
import tools.jackson.databind.json.JsonMapper;

/**
 * The log row of a LOG message is committed before the attachment is saved: a later failure must not make the consumer
 * handle the message again, or the log would be inserted twice.
 */
class LogMessageHandlerTest {

  private static final String BODY = """
      {"left": {"uuid": "log-uuid", "itemUuid": "item-uuid", "launchUuid": "launch-uuid",
                "message": "message", "level": "error", "file": {"name": "file.txt"}},
       "right": {"fileId": "file-id", "thumbnailFileId": "thumbnail-id", "contentType": "text/plain",
                 "fileSize": 1}}
      """;

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);
  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
  private final LogRepository logRepository = mock(LogRepository.class);
  private final TestItemService testItemService = mock(TestItemService.class);
  private final AttachmentBinaryDataService attachmentBinaryDataService =
      mock(AttachmentBinaryDataService.class);
  private final LogService logService = mock(LogService.class);
  private final LogTypeResolver logTypeResolver = mock(LogTypeResolver.class);

  private final LogMessageHandler handler = new LogMessageHandler(launchRepository, testItemRepository,
      logRepository, testItemService, attachmentBinaryDataService, logService, JsonMapper.builder().build(),
      logTypeResolver);

  private final MessageListener consumer = new ReportingConsumer(
      new ReportingHandlerProvider(Map.of(RequestType.LOG, handler)));

  @BeforeEach
  void setUp() {
    TestItem item = new TestItem();
    item.setItemId(2L);
    item.setUuid("item-uuid");
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUuid("launch-uuid");
    when(testItemRepository.findByUuid("item-uuid")).thenReturn(Optional.of(item));
    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);
  }

  @Test
  void lockConflictAfterTheLogIsSavedIsNotRetried() {
    CannotAcquireLockException lockConflict = new CannotAcquireLockException("lock");
    doThrow(lockConflict).when(attachmentBinaryDataService).attachToLog(any(), any());

    LogAlreadySavedException thrown = assertThrows(LogAlreadySavedException.class,
        () -> consumer.onMessage(message()));

    assertSame(lockConflict, thrown.getCause());
    verify(logRepository, times(1)).save(any(Log.class));
  }

  @Test
  void lockConflictBeforeTheLogIsSavedIsRetried() {
    when(testItemRepository.findByUuid("item-uuid"))
        .thenThrow(new CannotAcquireLockException("lock"))
        .thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> consumer.onMessage(message()));

    verify(testItemRepository, times(2)).findByUuid("item-uuid");
    verify(logRepository, never()).save(any(Log.class));
  }

  private static Message message() {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.LOG.name());
    properties.setHeader(MessageHeaders.PROJECT_ID, 1L);
    return new Message(BODY.getBytes(StandardCharsets.UTF_8), properties);
  }
}
