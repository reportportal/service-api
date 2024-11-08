/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.reporting.async.handler;

import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_FULL_TO_LOG;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.message.MessageRetriever;
import com.epam.ta.reportportal.ws.converter.builders.LogFullBuilder;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class LogMessageHandler implements ReportingMessageHandler {

  private static final Logger LOGGER = LogManager.getLogger(LogMessageHandler.class);
  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;
  private final LogRepository logRepository;
  private final TestItemService testItemService;
  private final AttachmentBinaryDataService attachmentBinaryDataService;
  private final LogService logService;
  private final ObjectMapper objectMapper;

  public LogMessageHandler(LaunchRepository launchRepository, TestItemRepository testItemRepository,
      LogRepository logRepository, TestItemService testItemService,
      AttachmentBinaryDataService attachmentBinaryDataService, LogService logService,
      ObjectMapper objectMapper) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.logRepository = logRepository;
    this.testItemService = testItemService;
    this.attachmentBinaryDataService = attachmentBinaryDataService;
    this.logService = logService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void handleMessage(Message message) {
    String incomeMessage = new String(message.getBody(), StandardCharsets.UTF_8);
    Optional<DeserializablePair<SaveLogRQ, BinaryDataMetaInfo>> payload = retrieveMessage(
        incomeMessage);
    payload.ifPresent(p -> {
      Map<String, Object> headers = message.getMessageProperties().getHeaders();
      Long projectId = (Long) headers.get(MessageHeaders.PROJECT_ID);
      SaveLogRQ rq = p.getLeft();
      BinaryDataMetaInfo metaInfo = p.getRight();
      handleLog(rq, metaInfo, projectId);
    });
  }

  private Optional<DeserializablePair<SaveLogRQ, BinaryDataMetaInfo>> retrieveMessage(
      String message) {
    JavaType javaType = objectMapper.getTypeFactory()
        .constructParametricType(DeserializablePair.class, SaveLogRQ.class,
            BinaryDataMetaInfo.class);
    try {
      return Optional.of(objectMapper.readValue(
          message, javaType));
    } catch (JsonProcessingException e) {
      LOGGER.error("Incorrect json format of incoming message. Discarded message: {}",
          message);
    }
    return Optional.empty();
  }

  private void handleLog(SaveLogRQ request, BinaryDataMetaInfo metaInfo, Long projectId) {
    Optional<TestItem> itemOptional = testItemRepository.findByUuid(request.getItemUuid());

    if (StringUtils.isNotEmpty(request.getItemUuid()) && itemOptional.isEmpty()) {
      throw new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, request.getItemUuid());
    }

    if (itemOptional.isPresent()) {
      createItemLog(request, itemOptional.get(), metaInfo, projectId);
    } else {
      Launch launch = launchRepository.findByUuid(request.getLaunchUuid())
          .orElseThrow(
              () -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, request.getLaunchUuid()));
      createLaunchLog(request, launch, metaInfo, projectId);
    }
  }

  private void createItemLog(SaveLogRQ request, TestItem item, BinaryDataMetaInfo metaInfo,
      Long projectId) {
    LogFull logFull = new LogFullBuilder().addSaveLogRq(request).addTestItem(item)
        .addProjectId(projectId).get();
    Log log = LOG_FULL_TO_LOG.apply(logFull);
    logRepository.save(log);
    logFull.setId(log.getId());
    Launch effectiveLaunch = testItemService.getEffectiveLaunch(item);
    logService.saveLogMessage(logFull, effectiveLaunch.getId());

    if (request.getFile() != null) {
      saveAttachment(request.getFile().getName(), metaInfo,
          logFull.getId(),
          projectId,
          effectiveLaunch.getId(),
          item.getItemId(),
          effectiveLaunch.getUuid(),
          logFull.getUuid()
      );
    }
  }

  private void createLaunchLog(SaveLogRQ request, Launch launch, BinaryDataMetaInfo metaInfo,
      Long projectId) {
    LogFull logFull = new LogFullBuilder().addSaveLogRq(request).addLaunch(launch)
        .addProjectId(projectId).get();
    Log log = LOG_FULL_TO_LOG.apply(logFull);
    logRepository.save(log);
    logFull.setId(log.getId());
    logService.saveLogMessage(logFull, launch.getId());

    if (request.getFile() != null) {
      saveAttachment(request.getFile().getName(), metaInfo, logFull.getId(), projectId,
          launch.getId(),
          null, launch.getUuid(),
          logFull.getUuid());
    }
  }

  private void saveAttachment(String fileName, BinaryDataMetaInfo metaInfo, Long logId,
      Long projectId, Long launchId, Long itemId, String launchUuid,
      String logUuid) {
    if (!Objects.isNull(metaInfo)) {
      attachmentBinaryDataService.attachToLog(metaInfo,
          AttachmentMetaInfo.builder()
              .withProjectId(projectId)
              .withLaunchId(launchId)
              .withItemId(itemId)
              .withLogId(logId)
              .withLaunchUuid(launchUuid)
              .withLogUuid(logUuid)
              .withFileName(fileName)
              .withCreationDate(Instant.now())
              .build()
      );
    }
  }
}
