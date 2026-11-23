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

package com.epam.reportportal.base.reporting.async.exception;

import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.REPORTING_PARKING_LOT;
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.RETRY_EXCHANGE;
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.TTL_QUEUE_M;
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.TTL_QUEUE_MS;
import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.TTL_QUEUE_S;

import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.config.RequestType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

/**
 * Error handler for RabbitMQ reporting listeners that decides whether a failed message should be retried or discarded.
 *
 * <p>The container nacks the original message without requeue after this handler returns, so the copy published here
 * is the only one left. It is published with a publisher confirm; if that fails, the handler throws
 * {@link ImmediateRequeueAmqpException} so that the container requeues the original instead of dropping it.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
public class ReportingErrorHandler implements ErrorHandler {

  static final String RETRIED_METRIC = "reporting.messages.retried";
  static final String PARKED_METRIC = "reporting.messages.parked";
  static final long CONFIRM_TIMEOUT_MS = 5_000;

  private static final long REQUEUE_DELAY_MS = 1_000;
  private static final Duration DEFAULT_PARKED_PARENTS_TTL = Duration.ofMinutes(20);
  private static final long DEFAULT_PARKED_PARENTS_MAX_SIZE = 10_000;

  private final RabbitTemplate rabbitTemplate;

  private final MeterRegistry meterRegistry;

  private final String RETRY_COUNT_HEADER = "x-retry-count";

  @Value("${reporting.retry.max-count:20}")
  private Integer maxRetryCount;

  /**
   * Retry budget of a message whose launch or parent item was not found. The default lets it wait through the
   * millisecond and second tiers and two minute-tier delays (about 4.5 minutes) instead of the full budget, so the
   * children of a rejected parent reach the parking lot without cycling through the retry queues for 18 minutes. A
   * child is retried in the same queue right after its parent, so it outlives a parent that needs up to two
   * minute-tier retries itself. The budget counts all retries of the message, not only the NOT_FOUND ones.
   */
  @Value("${reporting.retry.not-found-max-count:13}")
  private Integer notFoundMaxRetryCount;

  /**
   * UUIDs of launches and items whose start message was parked. A NOT_FOUND failure of a message that refers to one of
   * them is parked at once instead of waiting through the NOT_FOUND budget, and a parked child item is added in turn,
   * so the whole subtree follows its parent. The queue of a launch has one consumer, so a parent and its children are
   * handled by the same instance; when they are not (the hash ring changed), the children fall back to the budget.
   */
  private final Cache<String, Boolean> parkedParents;

  @Autowired
  public ReportingErrorHandler(RabbitTemplate rabbitTemplate,
      ObjectProvider<MeterRegistry> meterRegistry,
      @Value("${reporting.retry.parked-parents.ttl:PT20M}") Duration parkedParentsTtl,
      @Value("${reporting.retry.parked-parents.max-size:10000}") long parkedParentsMaxSize) {
    this(rabbitTemplate, meterRegistry.getIfAvailable(() -> Metrics.globalRegistry), parkedParentsTtl,
        parkedParentsMaxSize);
  }

  ReportingErrorHandler(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
    this(rabbitTemplate, meterRegistry, DEFAULT_PARKED_PARENTS_TTL, DEFAULT_PARKED_PARENTS_MAX_SIZE);
  }

  ReportingErrorHandler(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry,
      Duration parkedParentsTtl, long parkedParentsMaxSize) {
    this.rabbitTemplate = rabbitTemplate;
    this.meterRegistry = meterRegistry;
    this.parkedParents = Caffeine.newBuilder()
        .expireAfterAccess(parkedParentsTtl)
        .maximumSize(parkedParentsMaxSize)
        .build();
  }

  @Override
  public void handleError(Throwable t) {
    if (!(t instanceof ListenerExecutionFailedException executionFailedException)
        || executionFailedException.getFailedMessage() == null) {
      log.error("Reporting listener failed outside of message processing, requeueing the message", t);
      requeue("Unexpected reporting listener failure", t);
      return;
    }
    Message failedMessage = executionFailedException.getFailedMessage();
    MessageProperties properties = failedMessage.getMessageProperties();
    Throwable cause = Optional.ofNullable(executionFailedException.getCause())
        .orElse(executionFailedException);
    properties.getHeaders().put("exception", String.valueOf(cause.getMessage()));

    Object requestType = properties.getHeader(MessageHeaders.REQUEST_TYPE);
    Object launchUuid = properties.getHeader(MessageHeaders.HASH_ON);
    String reason = cause.getClass().getSimpleName();

    int retryCount = getRetryCount(properties);
    if (checkRetryExceeded(retryCount)) {
      log.warn("Number of retries exceeded max {} retry count. Rejecting message to parking lot queue. "
          + "Request Type: {}, Launch UUID: {}, Cause: {}", maxRetryCount, requestType, launchUuid, reason);
      park(failedMessage, requestType, "retries_exceeded");
      return;
    }

    RequestType type = toRequestType(requestType);
    boolean notFoundOnly = ReportingFailureClassifier.isRetryableOnlyAsNotFound(cause, type);
    if (notFoundOnly) {
      Optional<String> parkedParent = findParkedParent(properties, type);
      if (parkedParent.isPresent()) {
        log.warn("Launch or parent item '{}' of the message was parked. Rejecting message to parking lot queue. "
            + "Request Type: {}, Launch UUID: {}, Cause: {}", parkedParent.get(), requestType, launchUuid, reason);
        park(failedMessage, requestType, "parent_parked");
        return;
      }
    }

    if (retryCount >= notFoundMaxRetryCount && notFoundOnly) {
      log.warn("Launch or parent item of the message was not found after {} retries. Rejecting message to parking lot queue. "
          + "Request Type: {}, Launch UUID: {}, Cause: {}", retryCount, requestType, launchUuid, reason);
      park(failedMessage, requestType, "not_found_retries_exceeded");
      return;
    }

    if (ReportingFailureClassifier.isRetryable(cause, type)) {
      log.warn("Retrying reporting message. Attempt count is {}. Request Type: {}, Launch UUID: {}, Cause: {}",
          retryCount + 1, requestType, launchUuid, reason);
      properties.setHeader(RETRY_COUNT_HEADER, retryCount + 1);
      publishWithConfirm(RETRY_EXCHANGE, getRetryTtlQueue(retryCount), failedMessage);
      meterRegistry.counter(RETRIED_METRIC, "requestType", String.valueOf(requestType), "reason", reason)
          .increment();
      return;
    }

    log.error("Message rejected to the parking lot queue. Request Type: {}, Launch UUID: {}",
        requestType, launchUuid, cause);
    park(failedMessage, requestType, reason);
  }

  private void park(Message failedMessage, Object requestType, String reason) {
    publishWithConfirm("", REPORTING_PARKING_LOT, failedMessage);
    meterRegistry.counter(PARKED_METRIC, "requestType", String.valueOf(requestType), "reason", reason)
        .increment();
    MessageProperties properties = failedMessage.getMessageProperties();
    switch (toRequestType(requestType)) {
      case START_LAUNCH -> headerValue(properties, MessageHeaders.HASH_ON)
          .ifPresent(uuid -> parkedParents.put(uuid, Boolean.TRUE));
      case START_TEST -> headerValue(properties, MessageHeaders.ITEM_ID)
          .ifPresent(uuid -> parkedParents.put(uuid, Boolean.TRUE));
      case null, default -> {
      }
    }
  }

  /**
   * @return the parked launch or parent item the message refers to: its launch, and the parent item of a START_TEST or
   * the item of a FINISH_TEST or LOG
   */
  private Optional<String> findParkedParent(MessageProperties properties, RequestType type) {
    if (type == null || type == RequestType.START_LAUNCH) {
      return Optional.empty();
    }
    Optional<String> item = switch (type) {
      case START_TEST -> headerValue(properties, MessageHeaders.PARENT_ITEM_ID);
      case FINISH_TEST, LOG -> headerValue(properties, MessageHeaders.ITEM_ID);
      default -> Optional.empty();
    };
    return Stream.of(headerValue(properties, MessageHeaders.HASH_ON), item)
        .flatMap(Optional::stream)
        .filter(uuid -> parkedParents.getIfPresent(uuid) != null)
        .findFirst();
  }

  private static Optional<String> headerValue(MessageProperties properties, String header) {
    return Optional.ofNullable(properties.getHeader(header))
        .map(String::valueOf)
        .filter(value -> !value.isEmpty());
  }

  private void publishWithConfirm(String exchange, String routingKey, Message message) {
    try {
      rabbitTemplate.invoke(operations -> {
        operations.send(exchange, routingKey, message);
        operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
        return null;
      });
    } catch (RuntimeException e) {
      log.error("Unable to republish failed reporting message to '{}', requeueing it", routingKey, e);
      requeue("Republishing of the failed reporting message failed", e);
    }
  }

  private void requeue(String reason, Throwable cause) {
    try {
      // avoid a hot redelivery loop while the broker or the database is unavailable
      Thread.sleep(REQUEUE_DELAY_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    throw new ImmediateRequeueAmqpException(reason, cause);
  }

  private RequestType toRequestType(Object requestType) {
    return Arrays.stream(RequestType.values())
        .filter(type -> type.name().equals(String.valueOf(requestType)))
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the name of the retry TTL queue based on the number of retry attempts.
   *
   * <p>Selection logic:
   * <ul>
   *   <li>If {@code retryCount} is less than or equal to 5 — use the queue with millisecond TTL ({@code TTL_QUEUE_MS}).</li>
   *   <li>If {@code retryCount} is greater than 5 but less than or equal to 10 — use the queue with second TTL ({@code TTL_QUEUE_S}).</li>
   *   <li>If {@code retryCount} is greater than 10 — use the queue with minute TTL ({@code TTL_QUEUE_M}).</li>
   * </ul>
   *
   * @param retryCount the number of retry attempts
   * @return the name of the appropriate TTL queue
   */
  private String getRetryTtlQueue(int retryCount) {
    if (retryCount <= 5) {
      return TTL_QUEUE_MS;
    }
    if (retryCount <= 10) {
      return TTL_QUEUE_S;
    }
    return TTL_QUEUE_M;
  }

  private int getRetryCount(MessageProperties messageProperties) {
    return Optional.ofNullable(messageProperties.getHeader(RETRY_COUNT_HEADER))
        .map(it -> (Integer) it)
        .orElse(0);
  }

  private boolean checkRetryExceeded(long retries) {
    return retries >= maxRetryCount;
  }
}
