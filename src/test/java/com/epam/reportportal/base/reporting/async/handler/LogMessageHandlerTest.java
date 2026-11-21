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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.configs.rabbit.DeserializablePair;
import com.epam.reportportal.base.core.item.TestItemService;
import com.epam.reportportal.base.core.log.LogService;
import com.epam.reportportal.base.infrastructure.persistence.binary.AttachmentBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.commons.BinaryDataMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.service.LogTypeResolver;
import com.epam.reportportal.base.reporting.SaveLogRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import tools.jackson.databind.json.JsonMapper;

class LogMessageHandlerTest {

  private final LaunchRepository launchRepository = mock(LaunchRepository.class);
  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
  private final LogRepository logRepository = mock(LogRepository.class);
  private final TestItemService testItemService = mock(TestItemService.class);
  private final AttachmentBinaryDataService attachmentBinaryDataService =
      mock(AttachmentBinaryDataService.class);
  private final LogService logService = mock(LogService.class);
  private final LogTypeResolver logTypeResolver = mock(LogTypeResolver.class);
  private final JsonMapper jsonMapper = JsonMapper.builder().build();

  private final LogMessageHandler handler = new LogMessageHandler(launchRepository,
      testItemRepository, logRepository, testItemService, attachmentBinaryDataService, logService,
      jsonMapper, logTypeResolver);

  @Test
  void launchLogOfAnotherProjectIsDropped() {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUuid("launch-uuid");
    launch.setProjectId(2L);
    when(launchRepository.findByUuid("launch-uuid")).thenReturn(Optional.of(launch));

    handler.handleMessage(message(1L, "launch-uuid", null));

    verifyNoInteractions(logRepository, logService, attachmentBinaryDataService);
  }

  @Test
  void itemLogOfAnotherProjectIsDropped() {
    TestItem item = new TestItem();
    item.setItemId(3L);
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setProjectId(2L);
    when(testItemRepository.findByUuid("item-uuid")).thenReturn(Optional.of(item));
    when(testItemService.getEffectiveLaunch(item)).thenReturn(launch);

    handler.handleMessage(message(1L, "launch-uuid", "item-uuid"));

    verifyNoInteractions(logRepository, logService, attachmentBinaryDataService);
  }

  @Test
  void launchLogOfOwnProjectIsSaved() {
    Launch launch = new Launch();
    launch.setId(1L);
    launch.setUuid("launch-uuid");
    launch.setProjectId(1L);
    when(launchRepository.findByUuid("launch-uuid")).thenReturn(Optional.of(launch));

    handler.handleMessage(message(1L, "launch-uuid", null));

    verify(logRepository).save(any(Log.class));
  }

  private Message message(Long projectId, String launchUuid, String itemUuid) {
    SaveLogRQ rq = new SaveLogRQ();
    rq.setUuid("log-uuid");
    rq.setLaunchUuid(launchUuid);
    rq.setItemUuid(itemUuid);
    rq.setLevel("error");
    rq.setMessage("message");
    rq.setLogTime(Instant.now());
    String body = jsonMapper.writeValueAsString(
        DeserializablePair.of(rq, (BinaryDataMetaInfo) null));
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.PROJECT_ID, projectId);
    return new Message(body.getBytes(StandardCharsets.UTF_8), properties);
  }
}
