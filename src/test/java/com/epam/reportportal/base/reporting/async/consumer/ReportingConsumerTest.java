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

package com.epam.reportportal.base.reporting.async.consumer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.config.RequestType;
import com.epam.reportportal.base.reporting.async.handler.ReportingMessageHandler;
import com.epam.reportportal.base.reporting.async.handler.provider.ReportingHandlerProvider;
import java.sql.SQLTransientConnectionException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

class ReportingConsumerTest {

  private final ReportingMessageHandler handler = mock(ReportingMessageHandler.class);
  private final ReportingConsumer consumer = new ReportingConsumer(
      new ReportingHandlerProvider(Map.of(RequestType.FINISH_TEST, handler)), new long[]{0, 0, 0});

  @Test
  void lockConflictIsRetriedInTheConsumerThread() {
    Message message = message();
    doThrow(new CannotAcquireLockException("lock"))
        .doThrow(new CannotAcquireLockException("lock"))
        .doNothing()
        .when(handler).handleMessage(message);

    consumer.onMessage(message);

    verify(handler, times(3)).handleMessage(message);
  }

  @Test
  void lockConflictIsPassedToErrorHandlerAfterRetries() {
    Message message = message();
    doThrow(new CannotAcquireLockException("lock")).when(handler).handleMessage(message);

    assertThrows(CannotAcquireLockException.class, () -> consumer.onMessage(message));

    verify(handler, times(4)).handleMessage(message);
  }

  @Test
  void otherFailureIsNotRetriedInTheConsumerThread() {
    Message message = message();
    doThrow(new DataIntegrityViolationException("duplicate")).when(handler).handleMessage(message);

    assertThrows(DataIntegrityViolationException.class, () -> consumer.onMessage(message));

    verify(handler, times(1)).handleMessage(message);
  }

  @Test
  void sqlExceptionWithoutStateIsPassedToErrorHandlerUnchanged() {
    Message message = message();
    CannotCreateTransactionException failure = new CannotCreateTransactionException("no connection",
        new SQLTransientConnectionException("timeout"));
    doThrow(failure).when(handler).handleMessage(message);

    CannotCreateTransactionException thrown = assertThrows(CannotCreateTransactionException.class,
        () -> consumer.onMessage(message));

    assertSame(failure, thrown);
    verify(handler, times(1)).handleMessage(message);
  }

  @Test
  void successfulMessageIsHandledOnce() {
    Message message = message();
    doNothing().when(handler).handleMessage(message);

    consumer.onMessage(message);

    verify(handler, times(1)).handleMessage(message);
  }

  private static Message message() {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.REQUEST_TYPE, RequestType.FINISH_TEST.name());
    return new Message("{}".getBytes(), properties);
  }
}
