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

package com.epam.ta.reportportal.reporting.async.producer;

import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.DEFAULT_CONSISTENT_HASH_ROUTING_KEY;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_EXCHANGE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.core.log.impl.SaveLogBinaryDataTaskAsync;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.config.RequestType;
import com.epam.ta.reportportal.ws.reporting.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class LogProducer implements CreateLogHandler {

  @Autowired
  private Provider<SaveLogBinaryDataTaskAsync> saveLogBinaryDataTask;

  @Autowired
  @Qualifier("saveLogsTaskExecutor")
  private TaskExecutor taskExecutor;

  @Autowired
  @Qualifier(value = "rabbitTemplate")
  private AmqpTemplate amqpTemplate;


  @Nonnull
  @Override
  public EntryCreatedAsyncRS createLog(@Nonnull SaveLogRQ request, @Nullable MultipartFile file,
      MembershipDetails membershipDetails) {

    validate(request);
    if (!StringUtils.hasText(request.getUuid())) {
      request.setUuid(UUID.randomUUID().toString());
    }

    if (file != null) {
      CompletableFuture.supplyAsync(saveLogBinaryDataTask.get()
              .withRequest(request)
              .withFile(file)
              .withProjectId(membershipDetails.getProjectId()), taskExecutor)
          .thenAccept(metaInfo -> sendMessage(request, metaInfo, membershipDetails.getProjectId()));
    } else {
      sendMessage(request, null, membershipDetails.getProjectId());
    }

    EntryCreatedAsyncRS response = new EntryCreatedAsyncRS();
    response.setId(request.getUuid());
    return response;
  }

  public void sendMessage(SaveLogRQ request, BinaryDataMetaInfo metaInfo, Long projectId) {
    final String launchUuid = ofNullable(request.getLaunchUuid()).orElseThrow(
        () -> new ReportPortalException(
            ErrorType.BAD_REQUEST_ERROR, "Launch UUID should not be null or empty."));
    amqpTemplate.convertAndSend(
        REPORTING_EXCHANGE,
        DEFAULT_CONSISTENT_HASH_ROUTING_KEY,
        DeserializablePair.of(request, metaInfo),
        message -> {
          Map<String, Object> headers = message.getMessageProperties().getHeaders();
          headers.put(MessageHeaders.HASH_ON, launchUuid);
          headers.put(MessageHeaders.REQUEST_TYPE, RequestType.LOG);
          headers.put(MessageHeaders.PROJECT_ID, projectId);
          headers.put(MessageHeaders.ITEM_ID, request.getItemUuid());
          return message;
        }
    );

  }

}
