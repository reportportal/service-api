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

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ReportingErrorHandler implements RabbitListenerErrorHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ReportingErrorHandler.class);
  private static final List<ErrorType> RETRYABLE_ERROR_TYPES = Lists.newArrayList(
      ErrorType.LAUNCH_NOT_FOUND,
      ErrorType.TEST_SUITE_NOT_FOUND,
      ErrorType.TEST_ITEM_NOT_FOUND);
  private final RabbitTemplate rabbitTemplate;

  public ReportingErrorHandler(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public Object handleError(Message amqpMessage, org.springframework.messaging.Message<?> message,
      ListenerExecutionFailedException exception) {
    Throwable exceptionCause = exception.getCause();
    if (exceptionCause instanceof ReportPortalException) {
      ErrorType errorType = ((ReportPortalException) exceptionCause).getErrorType();
      if (RETRYABLE_ERROR_TYPES.contains(errorType)) {
        throw exception;
      }
    }
    LOGGER.error(exception.getCause().getMessage());
    LOGGER.error("Message rejected to the parking lot queue: {}",
        new String(amqpMessage.getBody()));
    amqpMessage.getMessageProperties().getHeaders()
        .put("exception", exception.getCause().getMessage());
    rabbitTemplate.send(REPORTING_PARKING_LOT, amqpMessage);
    return null;
  }
}
