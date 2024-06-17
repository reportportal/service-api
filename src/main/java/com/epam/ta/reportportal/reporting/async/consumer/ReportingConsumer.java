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

package com.epam.ta.reportportal.reporting.async.consumer;

import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.config.RequestType;
import com.epam.ta.reportportal.reporting.async.handler.ReportingMessageHandler;
import com.epam.ta.reportportal.reporting.async.handler.provider.ReportingHandlerProvider;
import java.util.Optional;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ReportingConsumer implements MessageListener {

  private final ReportingHandlerProvider handlerProvider;

  public ReportingConsumer(ReportingHandlerProvider handlerProvider) {
    this.handlerProvider = handlerProvider;
  }

  @Override
  public void onMessage(Message message) {
    RequestType requestType = getRequestType(message);
    Optional<ReportingMessageHandler> messageHandler = handlerProvider.provideHandler(requestType);
    messageHandler.ifPresent(handler -> handler.handleMessage(message));
  }

  private RequestType getRequestType(Message message) {
    return RequestType.valueOf(
        (String) message.getMessageProperties().getHeaders().get(MessageHeaders.REQUEST_TYPE));
  }
}
