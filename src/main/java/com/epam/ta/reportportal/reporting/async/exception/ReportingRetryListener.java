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

import static com.epam.ta.reportportal.reporting.async.config.MessageHeaders.XD_HEADER;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_EXCHANGE;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.REPORTING_PARKING_LOT;
import static com.epam.ta.reportportal.reporting.async.config.ReportingTopologyConfiguration.RETRY_QUEUE;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ReportingRetryListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ReportingRetryListener.class);
  private final RabbitTemplate rabbitTemplate;
  @Value("${reporting.retry.max-count:10}")
  private Integer maxRetryCount;

  public ReportingRetryListener(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @RabbitListener(queues = RETRY_QUEUE)
  public void receiveRejectedMessage(Message message,
      @Header(required = false, name = XD_HEADER) Map<String, ?> xDeath) {

    long retryCount = getRetryCount(xDeath);
    LOGGER.warn("Retrying reporting message. Attempt count is {}.", retryCount);

    if (checkRetryExceeded(retryCount)) {
      LOGGER.warn("Number of retries exceeded max {} retry count.", maxRetryCount);
      LOGGER.error("Rejecting message to parking lot queue.");
      rabbitTemplate.send(REPORTING_PARKING_LOT, message);
      return;
    }
    rabbitTemplate.send(REPORTING_EXCHANGE,
        message.getMessageProperties().getReceivedRoutingKey(), message);
  }

  private long getRetryCount(Map<String, ?> xDeath) {
    if (!CollectionUtils.isEmpty(xDeath)) {
      return (long) xDeath.get("count");
    }
    return 0;
  }

  private boolean checkRetryExceeded(long retries) {
    return retries >= maxRetryCount;
  }

}
