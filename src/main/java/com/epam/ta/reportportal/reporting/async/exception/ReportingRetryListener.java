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

import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.consumer.ReportingConsumer;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */

@Slf4j
//@Component
@RequiredArgsConstructor
public class ReportingRetryListener implements MessageListener {

  private final RabbitTemplate rabbitTemplate;

  private final ReportingConsumer reportingConsumer;

  @Value("${reporting.retry.max-count:10}")
  private Integer maxRetryCount;

  @Override
  public void onMessage(Message message) {
    long retryCount = getRetryCount(message.getMessageProperties().getXDeathHeader().getFirst());
    log.warn(
        "Retrying reporting message. Attempt count is {}. Request Type: {}, Launch UUID: {} ",
        retryCount,
        message.getMessageProperties().getHeader(MessageHeaders.REQUEST_TYPE),
        message.getMessageProperties().getHeader(MessageHeaders.HASH_ON));

    if (checkRetryExceeded(retryCount)) {
      log.warn("Number of retries exceeded max {} retry count.", maxRetryCount);
      log.warn("Rejecting message to parking lot queue. Message: {}",
          new String(message.getBody()));
      rabbitTemplate.send(REPORTING_PARKING_LOT, message);
      return;
    }
    reportingConsumer.onMessage(message);
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
