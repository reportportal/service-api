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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import java.util.Arrays;
import java.util.Optional;
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

  private final RabbitTemplate rabbitTemplate;

  private final MeterRegistry meterRegistry;

  private final String RETRY_COUNT_HEADER = "x-retry-count";

  @Value("${reporting.retry.max-count:20}")
  private Integer maxRetryCount;

  /**
   * Retry budget of a message whose launch or parent item was not found. The default lets it wait through the
   * millisecond and second tiers and one minute-tier delay (about 2.5 minutes) instead of the full budget, so the
   * children of a rejected parent reach the parking lot without cycling through the retry queues for 18 minutes.
   */
  @Value("${reporting.retry.not-found-max-count:12}")
  private Integer notFoundMaxRetryCount;

  @Autowired
  public ReportingErrorHandler(RabbitTemplate rabbitTemplate,
      ObjectProvider<MeterRegistry> meterRegistry) {
    this(rabbitTemplate, meterRegistry.getIfAvailable(() -> Metrics.globalRegistry));
  }

  ReportingErrorHandler(RabbitTemplate rabbitTemplate, MeterRegistry meterRegistry) {
    this.rabbitTemplate = rabbitTemplate;
    this.meterRegistry = meterRegistry;
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
    if (retryCount >= notFoundMaxRetryCount
        && ReportingFailureClassifier.isRetryableOnlyAsNotFound(cause, type)) {
      log.warn("Parent of the message was not found after {} retries. Rejecting message to parking lot queue. "
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
