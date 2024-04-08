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

import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_FULL_TO_LOG;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LogFullBuilder;
import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Create log handler. Save log and binary data related to it
 *
 * @author Henadzi Vrubleuski
 * @author Andrei Varabyeu
 */
@Service
@Primary
@Transactional
public class CreateLogHandlerImpl implements CreateLogHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateLogHandlerImpl.class);

  @Autowired
  TestItemRepository testItemRepository;

  @Autowired
  TestItemService testItemService;

  @Autowired
  LaunchRepository launchRepository;

  @Autowired
  LogRepository logRepository;

  @Autowired
  AttachmentBinaryDataService attachmentBinaryDataService;

  @Autowired
  private LogService logService;

  @Autowired
  @Qualifier("saveLogsTaskExecutor")
  private TaskExecutor taskExecutor;

  @Override
  @Nonnull
  //TODO check saving an attachment of the item of the project A in the project's B directory
  public EntryCreatedAsyncRS createLog(@Nonnull SaveLogRQ request, MultipartFile file,
      ReportPortalUser.ProjectDetails projectDetails) {
    validate(request);

    final LogFullBuilder logFullBuilder =
        new LogFullBuilder().addSaveLogRq(request).addProjectId(projectDetails.getProjectId());

    final Launch launch = testItemRepository.findByUuid(request.getItemUuid()).map(item -> {
      logFullBuilder.addTestItem(item);
      return testItemService.getEffectiveLaunch(item);
    }).orElseGet(() -> launchRepository.findByUuid(request.getLaunchUuid()).map(l -> {
      logFullBuilder.addLaunch(l);
      return l;
    }).orElseThrow(
        () -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, request.getLaunchUuid())));

    final LogFull logFull = logFullBuilder.get();
    final Log log = LOG_FULL_TO_LOG.apply(logFull);
    CompletableFuture.supplyAsync(() -> logRepository.saveAndFlush(log), taskExecutor)
        .thenAcceptAsync(savedLog -> {
          logFull.setId(savedLog.getId());
          logService.saveLogMessage(logFull, launch.getId());
          if (file != null) {
            saveBinaryData(file, launch, savedLog);
          }
        }, taskExecutor)
        .exceptionally(e -> {
              LOGGER.error("Failed to save log with attachments", e);
              return null;
            }
        );

    return new EntryCreatedAsyncRS(log.getUuid());
  }

  private void saveBinaryData(MultipartFile file, Launch launch, Log savedLog) {
    final AttachmentMetaInfo.AttachmentMetaInfoBuilder metaInfoBuilder =
        AttachmentMetaInfo.builder().withProjectId(launch.getProjectId())
            .withLaunchId(launch.getId()).withLaunchUuid(launch.getUuid())
            .withLogId(savedLog.getId())
            .withFileName(file.getOriginalFilename())
            .withLogUuid(savedLog.getUuid())
            .withCreationDate(Instant.now());
    ofNullable(savedLog.getTestItem()).map(TestItem::getItemId)
        .ifPresent(metaInfoBuilder::withItemId);

    attachmentBinaryDataService.saveFileAndAttachToLog(file, metaInfoBuilder.build());
  }
}
