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

package com.epam.ta.reportportal.reporting.async.exception;

import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_PARKING_LOT;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.RETRY_EXCHANGE;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.TTL_QUEUE;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ErrorHandler;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
public class ReportingErrorHandler implements ErrorHandler {

  private static final List<ErrorType> RETRYABLE_ERROR_TYPES = Lists.newArrayList(
      ErrorType.LAUNCH_NOT_FOUND,
      ErrorType.TEST_SUITE_NOT_FOUND,
      ErrorType.TEST_ITEM_NOT_FOUND);

  private final RabbitTemplate rabbitTemplate;

  @Value("${reporting.retry.max-count:10}")
  private Integer maxRetryCount;

  public ReportingErrorHandler(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public void handleError(Throwable t) {
    if (t instanceof ListenerExecutionFailedException executionFailedException) {
      Message failedMessage = executionFailedException.getFailedMessage();
      failedMessage.getMessageProperties().getHeaders()
          .put("exception", executionFailedException.getCause().getMessage());

      long retryCount = getRetryCount(failedMessage.getMessageProperties().getXDeathHeader());
      if (retryCount > 0) {
        log.warn(
            "Retrying reporting message. Attempt count is {}. Request Type: {}, Launch UUID: {} ",
            retryCount,
            failedMessage.getMessageProperties().getHeader(MessageHeaders.REQUEST_TYPE),
            failedMessage.getMessageProperties().getHeader(MessageHeaders.HASH_ON));
      }

      if (checkRetryExceeded(retryCount)) {
        log.warn("Number of retries exceeded max {} retry count.", maxRetryCount);
        log.warn("Rejecting message to parking lot queue. Message: {}",
            new String(failedMessage.getBody()));
        rabbitTemplate.send(REPORTING_PARKING_LOT, failedMessage);
        return;
      }

      if (executionFailedException.getCause() instanceof ReportPortalException reportPortalException) {
        if (RETRYABLE_ERROR_TYPES.contains(reportPortalException.getErrorType())) {
          rabbitTemplate.send(RETRY_EXCHANGE, TTL_QUEUE, failedMessage);
          return;
        }
      }
      log.error("Message rejected to the parking lot queue: {}",
          new String(failedMessage.getBody()));
      rabbitTemplate.send(REPORTING_PARKING_LOT, failedMessage);
    }
  }

  private long getRetryCount(List<Map<String, ?>> xDeathHeaders) {
    if (!CollectionUtils.isEmpty(xDeathHeaders)) {
      var xDeath = xDeathHeaders.getFirst();
      return Optional.ofNullable(xDeath.get("count")).map(count -> (long) count).orElse(0L);
    }
    return 0;
  }

  private boolean checkRetryExceeded(long retries) {
    return retries >= maxRetryCount;
  }
}
