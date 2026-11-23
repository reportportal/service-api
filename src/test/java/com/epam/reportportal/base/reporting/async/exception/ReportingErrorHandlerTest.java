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
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.TTL_QUEUE_M;
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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.RollbackException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
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
  private static final int NOT_FOUND_MAX_RETRY_COUNT = 13;

  private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
  private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
  private final ReportingErrorHandler errorHandler = new ReportingErrorHandler(rabbitTemplate,
      meterRegistry);

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    ReflectionTestUtils.setField(errorHandler, "maxRetryCount", MAX_RETRY_COUNT);
    ReflectionTestUtils.setField(errorHandler, "notFoundMaxRetryCount", NOT_FOUND_MAX_RETRY_COUNT);
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
            new AmqpConnectException(new RuntimeException("refused"))),
        Arguments.of("lock conflict after log row saved", RequestType.LOG,
            new LogAlreadySavedException("log-uuid", new CannotAcquireLockException("lock"))),
        Arguments.of("connection failure after log row saved", RequestType.LOG,
            new LogAlreadySavedException("log-uuid", new CannotCreateTransactionException("pool exhausted")))
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
  void notFoundReachingItsBudgetIsSentToParkingLot() {
    Message message = message(RequestType.LOG, NOT_FOUND_MAX_RETRY_COUNT);

    errorHandler.handleError(failure(message,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, message);
    verify(rabbitTemplate, never()).send(eq(RETRY_EXCHANGE), anyString(), any(Message.class));
    assertEquals(1.0, meterRegistry.get(ReportingErrorHandler.PARKED_METRIC)
        .tag("reason", "not_found_retries_exceeded").counter().count());
  }

  static Stream<Arguments> failuresBeyondTheNotFoundBudget() {
    return Stream.of(
        Arguments.of("duplicate key wrapping not found", RequestType.START_TEST, NOT_FOUND_MAX_RETRY_COUNT,
            new DataIntegrityViolationException("duplicate",
                new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")),
            "DataIntegrityViolationException"),
        Arguments.of("not found with broker failure on log", RequestType.LOG, NOT_FOUND_MAX_RETRY_COUNT,
            new AmqpTimeoutException("timeout",
                new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")),
            "not_found_retries_exceeded"),
        Arguments.of("NPE", RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT, new NullPointerException(),
            "NullPointerException"),
        Arguments.of("not found at the global cap", RequestType.FINISH_TEST, MAX_RETRY_COUNT,
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid"), "retries_exceeded"),
        Arguments.of("nested not found", RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT,
            new IllegalStateException(
                new RuntimeException(new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid"))),
            "not_found_retries_exceeded"),
        Arguments.of("not found well above the budget", RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT + 2,
            new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "uuid"), "not_found_retries_exceeded")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("failuresBeyondTheNotFoundBudget")
  void failureBeyondTheNotFoundBudgetIsParkedWithItsReason(String name, RequestType requestType,
      int retryCount, Throwable cause, String reason) {
    Message message = message(requestType, retryCount);

    errorHandler.handleError(failure(message, cause));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, message);
    verify(rabbitTemplate, never()).send(eq(RETRY_EXCHANGE), anyString(), any(Message.class));
    assertEquals(1.0, meterRegistry.get(ReportingErrorHandler.PARKED_METRIC).tag("reason", reason)
        .counter().count());
  }

  @Test
  void notFoundWithBrokerFailureOnFinishKeepsTheFullBudget() {
    Message message = message(RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT);

    errorHandler.handleError(failure(message, new AmqpTimeoutException("timeout",
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid"))));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_M, message);
  }

  @Test
  void notFoundBelowItsBudgetIsRetried() {
    Message message = message(RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT - 1);

    errorHandler.handleError(failure(message,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_M, message);
    verify(rabbitTemplate, never()).send(eq(""), eq(REPORTING_PARKING_LOT), any(Message.class));
  }

  @Test
  void notFoundWithTransientCauseKeepsTheFullBudget() {
    Message message = message(RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT);

    errorHandler.handleError(failure(message,
        new CannotAcquireLockException("lock",
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid"))));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_M, message);
    verify(rabbitTemplate, never()).send(eq(""), eq(REPORTING_PARKING_LOT), any(Message.class));
  }

  @Test
  void transientFailureBeyondTheNotFoundBudgetIsRetried() {
    Message message = message(RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT + 2);

    errorHandler.handleError(failure(message, new CannotAcquireLockException("lock")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_M, message);
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

  static Stream<Arguments> childrenOfAParkedParent() {
    return Stream.of(
        Arguments.of("child start of a parked item", RequestType.START_TEST, "launch-uuid", "parent",
            message(RequestType.START_TEST, null, "launch-uuid", "child", "parent"),
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")),
        Arguments.of("finish of a parked item", RequestType.START_TEST, "launch-uuid", "parent",
            message(RequestType.FINISH_TEST, 2, "launch-uuid", "parent", null),
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")),
        Arguments.of("log of a parked item", RequestType.START_TEST, "launch-uuid", "parent",
            message(RequestType.LOG, null, "launch-uuid", "parent", null),
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")),
        Arguments.of("root start of a parked launch", RequestType.START_LAUNCH, "parked-launch", null,
            message(RequestType.START_TEST, null, "parked-launch", "root", ""),
            new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "parked-launch")),
        Arguments.of("launch log of a parked launch", RequestType.START_LAUNCH, "parked-launch", null,
            message(RequestType.LOG, null, "parked-launch", null, null),
            new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "parked-launch")),
        Arguments.of("finish of a parked launch", RequestType.START_LAUNCH, "parked-launch", null,
            message(RequestType.FINISH_LAUNCH, null, "parked-launch", null, null),
            new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "parked-launch"))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("childrenOfAParkedParent")
  void notFoundChildOfAParkedParentIsParkedAtOnce(String name, RequestType parentType, String launchUuid,
      String parentUuid, Message child, Throwable cause) {
    parkParent(parentType, launchUuid, parentUuid);

    errorHandler.handleError(failure(child, cause));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, child);
    verify(rabbitTemplate, never()).send(eq(RETRY_EXCHANGE), anyString(), any(Message.class));
    assertEquals(1.0, meterRegistry.get(ReportingErrorHandler.PARKED_METRIC).tag("reason", "parent_parked")
        .counter().count());
  }

  @Test
  void unrelatedNotFoundIsRetriedAfterAParentWasParked() {
    parkParent(RequestType.START_TEST, "launch-uuid", "parent");
    Message other = message(RequestType.FINISH_TEST, null, "other-launch", "other-item", null);

    errorHandler.handleError(failure(other,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "other-item")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, other);
    verify(rabbitTemplate, never()).send("", REPORTING_PARKING_LOT, other);
  }

  @Test
  void parkedChildCascadesToGrandchildren() {
    parkParent(RequestType.START_TEST, "launch-uuid", "parent");
    Message child = message(RequestType.START_TEST, null, "launch-uuid", "child", "parent");
    errorHandler.handleError(failure(child,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")));
    Message grandchild = message(RequestType.START_TEST, null, "launch-uuid", "grandchild", "child");
    errorHandler.handleError(failure(grandchild,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "child")));
    Message grandchildLog = message(RequestType.LOG, null, "launch-uuid", "grandchild", null);

    errorHandler.handleError(failure(grandchildLog,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "grandchild")));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, child);
    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, grandchild);
    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, grandchildLog);
    verify(rabbitTemplate, never()).send(eq(RETRY_EXCHANGE), anyString(), any(Message.class));
    assertEquals(3.0, meterRegistry.get(ReportingErrorHandler.PARKED_METRIC).tag("reason", "parent_parked")
        .counters().stream().mapToDouble(Counter::count).sum());
  }

  @Test
  void itemParkedByTheNotFoundBudgetIsRecordedAsParkedParent() {
    Message parent = message(RequestType.START_TEST, NOT_FOUND_MAX_RETRY_COUNT, "launch-uuid", "parent", "missing");
    errorHandler.handleError(failure(parent,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "missing")));
    Message child = message(RequestType.FINISH_TEST, null, "launch-uuid", "parent", null);

    errorHandler.handleError(failure(child,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")));

    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, child);
    assertEquals(1.0, meterRegistry.get(ReportingErrorHandler.PARKED_METRIC).tag("reason", "parent_parked")
        .counter().count());
  }

  static Stream<Arguments> transientFailuresOfAChild() {
    return Stream.of(
        Arguments.of("lock conflict", new CannotAcquireLockException("lock")),
        Arguments.of("not found with a transient cause", new CannotAcquireLockException("lock",
            new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent"))),
        Arguments.of("broker timeout", new AmqpTimeoutException("timeout"))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("transientFailuresOfAChild")
  void transientFailureOfAChildOfAParkedParentKeepsTheFullBudget(String name, Throwable cause) {
    parkParent(RequestType.START_TEST, "launch-uuid", "parent");
    Message child = message(RequestType.FINISH_TEST, NOT_FOUND_MAX_RETRY_COUNT, "launch-uuid", "parent", null);

    errorHandler.handleError(failure(child, cause));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_M, child);
    verify(rabbitTemplate, never()).send("", REPORTING_PARKING_LOT, child);
  }

  @Test
  void startTestIsNotParkedForItsOwnParkedUuid() {
    parkParent(RequestType.START_TEST, "launch-uuid", "parent");
    Message replayed = message(RequestType.START_TEST, null, "launch-uuid", "parent", "other-parent");

    errorHandler.handleError(failure(replayed,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "other-parent")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, replayed);
  }

  @Test
  void parentIsNotRecordedWhenItsParkingFails() {
    Message parent = message(RequestType.START_TEST, null, "launch-uuid", "parent", "");
    doThrow(new AmqpConnectException(new RuntimeException("refused"))).doNothing().when(rabbitTemplate)
        .send(anyString(), anyString(), any(Message.class));
    assertThrows(ImmediateRequeueAmqpException.class,
        () -> errorHandler.handleError(failure(parent, new NullPointerException())));
    Message child = message(RequestType.FINISH_TEST, null, "launch-uuid", "parent", null);

    errorHandler.handleError(failure(child,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, child);
  }

  @Test
  void parkedParentIsForgottenAfterItsTtl() throws InterruptedException {
    ReportingErrorHandler shortLivedHandler = new ReportingErrorHandler(rabbitTemplate, meterRegistry,
        Duration.ofMillis(50), 100);
    ReflectionTestUtils.setField(shortLivedHandler, "maxRetryCount", MAX_RETRY_COUNT);
    ReflectionTestUtils.setField(shortLivedHandler, "notFoundMaxRetryCount", NOT_FOUND_MAX_RETRY_COUNT);
    shortLivedHandler.handleError(failure(message(RequestType.START_TEST, null, "launch-uuid", "parent", ""),
        new NullPointerException()));
    Thread.sleep(200);
    Message child = message(RequestType.FINISH_TEST, null, "launch-uuid", "parent", null);

    shortLivedHandler.handleError(failure(child,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "parent")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, child);
  }

  @Test
  void itemNotFoundInALaunchRecordedByADuplicateStartIsRetried() {
    Message duplicateStart = message(RequestType.START_LAUNCH, null, "live-launch", null, null);
    errorHandler.handleError(failure(duplicateStart,
        new DataIntegrityViolationException("duplicate", sqlException("23505"))));
    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, duplicateStart);
    Message childStart = message(RequestType.START_TEST, null, "live-launch", "child", "pending-parent");
    Message log = message(RequestType.LOG, null, "live-launch", "pending-item", null);

    errorHandler.handleError(failure(childStart,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "pending-parent")));
    errorHandler.handleError(failure(log,
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "pending-item")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, childStart);
    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, log);
    verify(rabbitTemplate, never()).send("", REPORTING_PARKING_LOT, childStart);
    verify(rabbitTemplate, never()).send("", REPORTING_PARKING_LOT, log);
  }

  @Test
  void launchNotFoundIsNotMatchedAgainstAParkedItem() {
    parkParent(RequestType.START_TEST, "launch-uuid", "parent");
    Message child = message(RequestType.FINISH_TEST, null, "pending-launch", "parent", null);

    errorHandler.handleError(failure(child,
        new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "pending-launch")));

    verify(rabbitTemplate).send(RETRY_EXCHANGE, TTL_QUEUE_MS, child);
  }

  @Test
  void notFoundErrorTypeIsTakenFromTheCauseChain() {
    assertEquals(Optional.of(ErrorType.LAUNCH_NOT_FOUND), ReportingFailureClassifier.notFoundErrorType(
        new IllegalStateException(new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "uuid"))));
    assertEquals(Optional.of(ErrorType.TEST_ITEM_NOT_FOUND), ReportingFailureClassifier.notFoundErrorType(
        new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, "uuid")));
    assertEquals(Optional.empty(), ReportingFailureClassifier.notFoundErrorType(
        new ReportPortalException(ErrorType.FINISH_ITEM_NOT_ALLOWED, "uuid")));
    assertEquals(Optional.empty(), ReportingFailureClassifier.notFoundErrorType(new NullPointerException()));
  }

  private void parkParent(RequestType type, String launchUuid, String itemUuid) {
    Message parent = message(type, null, launchUuid, itemUuid, "");
    errorHandler.handleError(failure(parent, new NullPointerException()));
    verify(rabbitTemplate).send("", REPORTING_PARKING_LOT, parent);
  }

  private static SQLException sqlException(String sqlState) {
    return new SQLException("sql failure " + sqlState, sqlState);
  }

  private static ListenerExecutionFailedException failure(Message message, Throwable cause) {
    return new ListenerExecutionFailedException("Listener threw exception", cause, message);
  }

  private static Message message(RequestType requestType, Integer retryCount) {
    return message(requestType, retryCount, "launch-uuid", null, null);
  }

  private static Message message(RequestType requestType, Integer retryCount, String launchUuid,
      String itemUuid, String parentItemUuid) {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.REQUEST_TYPE, requestType.name());
    properties.setHeader(MessageHeaders.HASH_ON, launchUuid);
    if (itemUuid != null) {
      properties.setHeader(MessageHeaders.ITEM_ID, itemUuid);
    }
    if (parentItemUuid != null) {
      properties.setHeader(MessageHeaders.PARENT_ITEM_ID, parentItemUuid);
    }
    if (retryCount != null) {
      properties.setHeader("x-retry-count", retryCount);
    }
    return new Message("{}".getBytes(), properties);
  }
}
