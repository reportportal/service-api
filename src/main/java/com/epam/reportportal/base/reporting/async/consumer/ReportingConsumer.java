/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.reporting.async.consumer;

import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.config.RequestType;
import com.epam.reportportal.base.reporting.async.exception.ReportingFailureClassifier;
import com.epam.reportportal.base.reporting.async.handler.ReportingMessageHandler;
import com.epam.reportportal.base.reporting.async.handler.provider.ReportingHandlerProvider;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * RabbitMQ message listener that receives reporting messages and dispatches them to the appropriate
 * {@link com.epam.reportportal.base.reporting.async.handler.ReportingMessageHandler}.
 * <p>
 * A lock conflict (deadlock, serialization failure, lock wait timeout) is retried a few times in the consumer thread
 * before the failure reaches the error handler: the consumer is the only one of its queue, so this keeps the order of
 * the messages of a launch, while the retry queues put the message behind later ones.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
public class ReportingConsumer implements MessageListener {

  private static final long[] LOCK_CONFLICT_RETRY_DELAYS_MS = {50, 200, 1000};

  private final ReportingHandlerProvider handlerProvider;

  private final long[] lockConflictRetryDelaysMs;

  public ReportingConsumer(ReportingHandlerProvider handlerProvider) {
    this(handlerProvider, LOCK_CONFLICT_RETRY_DELAYS_MS);
  }

  ReportingConsumer(ReportingHandlerProvider handlerProvider, long[] lockConflictRetryDelaysMs) {
    this.handlerProvider = handlerProvider;
    this.lockConflictRetryDelaysMs = lockConflictRetryDelaysMs;
  }

  @Override
  public void onMessage(Message message) {
    RequestType requestType = getRequestType(message);
    Optional<ReportingMessageHandler> messageHandler = handlerProvider.provideHandler(requestType);
    messageHandler.ifPresent(handler -> handleWithLockConflictRetry(handler, message, requestType));
  }

  private void handleWithLockConflictRetry(ReportingMessageHandler handler, Message message,
      RequestType requestType) {
    for (int attempt = 0; ; attempt++) {
      try {
        handler.handleMessage(message);
        return;
      } catch (RuntimeException e) {
        if (attempt >= lockConflictRetryDelaysMs.length || !ReportingFailureClassifier.isLockConflict(e)) {
          throw e;
        }
        log.warn("Lock conflict while handling reporting message, retrying in {} ms. Request Type: {}, "
                + "Launch UUID: {}, Cause: {}", lockConflictRetryDelaysMs[attempt], requestType,
            message.getMessageProperties().getHeader(MessageHeaders.HASH_ON), e.getClass().getSimpleName());
        sleep(lockConflictRetryDelaysMs[attempt], e);
      }
    }
  }

  private static void sleep(long delayMs, RuntimeException failure) {
    try {
      Thread.sleep(delayMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw failure;
    }
  }

  private RequestType getRequestType(Message message) {
    return RequestType.valueOf(
        (String) message.getMessageProperties().getHeaders().get(MessageHeaders.REQUEST_TYPE));
  }
}
