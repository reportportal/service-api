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

package com.epam.reportportal.base.reporting.async.exception;

import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.REPORTING_PARKING_LOT;
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.RETRY_EXCHANGE;
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.TTL_QUEUE_MS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.config.RequestType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.RollbackException;
import java.sql.SQLException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.core.RabbitOperations.OperationsCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;

class ReportingErrorHandlerTest {

  private static final int MAX_RETRY_COUNT = 20;

  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final ReportingErrorHandler errorHandler = new ReportingErrorHandler(rabbitTemplate,
      meterRegistry);

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    ReflectionTestUtils.setField(errorHandler, "maxRetryCount", MAX_RETRY_COUNT);
    when(rabbitTemplate.invoke(any(OperationsCallback.class))).thenAnswer(
        invocation -> ((OperationsCallback<Object>) invocation.getArgument(0)).doInRabbit(
            rabbitTemplate));
  }

  static Stream<Arguments> retryableFailures() {
    return Stream.of(
        Arguments.of("deadlock loser", RequestType.FINISH_TEST,
            new DeadlockLoserDataAccessException("deadlock", sqlException("40P01"))),
        Arguments.of("lock not acquired", RequestType.FINISH_TEST,
            new CannotAcquireLockException("lock", sqlException("55P03"))),
        Arguments.of("deadlock wrapped at commit", RequestType.FINISH_LAUNCH,
            new TransactionSystemException("commit failed",
                new RollbackException("rollback", sqlException("40P01")))),
        Arguments.of("deadlock in JpaSystemException", RequestType.START_TEST,
            new JpaSystemException(new RuntimeException(sqlException("40P01")))),
        Arguments.of("jOOQ connection failure", RequestType.FINISH_TEST,
            new org.jooq.exception.DataAccessException("connection", sqlException("08006"))),
        Arguments.of("statement timeout", RequestType.FINISH_TEST,
            new RuntimeException(sqlException("57014"))),
        Arguments.of("too many connections", RequestType.START_TEST,
            new RuntimeException(sqlException("53300"))),
        Arguments.of("database shutdown", RequestType.LOG,
            new DataAccessResourceFailureException("db down", sqlException("57P01"))),
        Arguments.of("no transaction", RequestType.LOG,
            new CannotCreateTransactionException("pool exhausted")),
        Arguments.of("lock conflict on log", RequestType.LOG, new CannotAcquireLockException("lock")),
        Arguments.of("broker connection", RequestType.FINISH_TEST,
            new AmqpConnectException(new RuntimeException("refused"))),
        Arguments.of("broker timeout", RequestType.START_LAUNCH, new AmqpTimeoutException("timeout")),
        Arguments.of("item not found", RequestType.FINISH_TEST,
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")),
        Arguments.of("item not found nested two levels deep", RequestType.LOG,
            new IllegalStateException(
                new RuntimeException(new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")))),
        Arguments.of("launch not found", RequestType.START_TEST,
            new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "uuid"))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("retryableFailures")
  void transientFailureIsSentToRetryQueue(String name, RequestType requestType, Throwable cause) {
    Message message = message(requestType, null);

    errorHandler.handleError(failure(message, cause));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, message);
    verify(rabbitTemplate).waitForConfirmsOrDie(ReportingErrorHandler.CONFIRM_TIMEOUT_MS);
    verify(rabbitTemplate, never()).send(eq(""), eq(REPORTING_PARKING_LOT), any(Message.class));
    assertEquals(1, (Integer) message.getMessageProperties().getHeader("x-retry-count"));
    assertEquals(1.0, meterRegistry.get(ReportingErrorHandler.RETRIED_METRIC).counter().count());
  }

  static Stream<Arguments> permanentFailures() {
    return Stream.of(
        Arguments.of("duplicate key", RequestType.START_TEST,
            new DataIntegrityViolationException("duplicate", sqlException("23505"))),
        Arguments.of("duplicate key wrapping a lock conflict", RequestType.START_TEST,
            new DataIntegrityViolationException("duplicate",
                new CannotAcquireLockException("lock"))),
        Arguments.of("other ReportPortalException", RequestType.FINISH_TEST,
            new ReportPortalException(ErrorType.FINISH_ITEM_NOT_ALLOWED, "not allowed")),
        Arguments.of("invalid API usage", RequestType.FINISH_TEST,
            new InvalidDataAccessApiUsageException("bad")),
        Arguments.of("NPE", RequestType.FINISH_LAUNCH, new NullPointerException()),
        Arguments.of("non transient SQL state", RequestType.FINISH_TEST,
            new RuntimeException(sqlException("42P01"))),
        Arguments.of("broker failure after log row saved", RequestType.LOG,
            new AmqpIOException(new java.io.IOException("channel closed"))),
        Arguments.of("broker connection on log", RequestType.LOG,
            new AmqpConnectException(new RuntimeException("refused")))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("permanentFailures")
  void permanentFailureIsSentToParkingLot(String name, RequestType requestType, Throwable cause) {
    Message message = message(requestType, null);

    errorHandler.handleError(failure(message, cause));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, message);
    verify(rabbitTemplate).waitForConfirmsOrDie(ReportingErrorHandler.CONFIRM_TIMEOUT_MS);
    verify(rabbitTemplate, never()).send(eq(RETRY_EXCHANGE), anyString(), any(Message.class));
    assertEquals(1.0, meterRegistry.get(ReportingErrorHandler.PARKED_METRIC).counter().count());
  }

  @Test
  void retryCountReachingMaxIsSentToParkingLot() {
    Message message = message(RequestType.FINISH_TEST, MAX_RETRY_COUNT);

    errorHandler.handleError(failure(message, new CannotAcquireLockException("lock")));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, message);
    verify(rabbitTemplate, never()).send(eq(RETRY_EXCHANGE), anyString(), any(Message.class));
  }

  @Test
  void retryCountIsIncrementedOnTheSharedHeader() {
    Message message = message(RequestType.FINISH_TEST, 7);

    errorHandler.handleError(failure(message,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")));

    assertEquals(8, (Integer) message.getMessageProperties().getHeader("x-retry-count"));
    verify(rabbitTemplate).send(eq(RETRY_EXCHANGE), anyString(), eq(message));
  }

  @Test
  void failedConfirmRequeuesOriginalMessage() {
    Message message = message(RequestType.FINISH_TEST, null);
    doThrow(new AmqpTimeoutException("no confirm")).when(rabbitTemplate)
        .waitForConfirmsOrDie(anyLong());

    assertThrows(ImmediateRequeueAmqpException.class,
        () -> errorHandler.handleError(failure(message, new CannotAcquireLockException("lock"))));
  }

  @Test
  void failedParkingLotPublishRequeuesOriginalMessage() {
    Message message = message(RequestType.START_TEST, null);
    doThrow(new AmqpConnectException(new RuntimeException("refused"))).when(rabbitTemplate)
        .send(anyString(), anyString(), any(Message.class));

    assertThrows(ImmediateRequeueAmqpException.class,
        () -> errorHandler.handleError(failure(message, new NullPointerException())));
  }

  @Test
  void failureWithoutCauseDoesNotThrowNullPointerException() {
    Message message = message(RequestType.FINISH_TEST, null);

    assertDoesNotThrow(() -> errorHandler.handleError(
        new ListenerExecutionFailedException("failed", null, message)));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, message);
  }

  @Test
  void failureOutsideOfMessageProcessingIsRequeued() {
    assertThrows(ImmediateRequeueAmqpException.class,
        () -> errorHandler.handleError(new IllegalStateException("container failure")));
    verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
  }

  private static SQLException sqlException(String sqlState) {
    return new SQLException("sql failure " + sqlState, sqlState);
  }

  private static ListenerExecutionFailedException failure(Message message, Throwable cause) {
    return new ListenerExecutionFailedException("Listener threw exception", cause, message);
  }

  private static Message message(RequestType requestType, Integer retryCount) {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.REQUEST_TYPE, requestType.name());
    properties.setHeader(MessageHeaders.HASH_ON, "launch-uuid");
    if (retryCount != null) {
      properties.setHeader("x-retry-count", retryCount);
    }
    return new Message("{}".getBytes(), properties);
  }
}
