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

package com.epam.reportportal.base.reporting.async.handler;

import static com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration.LAUNCH_FINISH_DELAY_QUEUE;

import com.epam.reportportal.base.auth.userdetails.DefaultUserDetailsService;
import com.epam.reportportal.base.core.launch.FinishLaunchHandler;
import com.epam.reportportal.base.core.launch.LaunchFinishGracePeriod;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.message.MessageRetriever;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Handles RabbitMQ messages for finishing a launch.
 *
 * <p>A finish message of a launch that still has items in progress is delayed once, for the rest of the
 * {@link LaunchFinishGracePeriod grace period} counted from the moment the API received the request: it is moved to
 * the {@link com.epam.reportportal.base.reporting.async.config.ReportingTopologyConfiguration#LAUNCH_FINISH_DELAY_QUEUE
 * delay queue}, which returns it to the queue of the launch when it expires. Meanwhile the finish messages of the
 * children that arrive late are processed. The end time of the launch is set by the API when the request is received
 * and does not depend on the delay.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Service
public class LaunchFinishMessageHandler implements ReportingMessageHandler {

  static final long CONFIRM_TIMEOUT_MS = 5_000;

  private final MessageRetriever retriever;
  private final FinishLaunchHandler finishLaunchHandler;
  private final ProjectExtractor projectExtractor;
  private final DefaultUserDetailsService userDetailsService;
  private final LaunchFinishGracePeriod launchFinishGracePeriod;
  private final RabbitTemplate rabbitTemplate;

  public LaunchFinishMessageHandler(MessageRetriever retriever,
      FinishLaunchHandler finishLaunchHandler,
      ProjectExtractor projectExtractor, DefaultUserDetailsService userDetailsService,
      LaunchFinishGracePeriod launchFinishGracePeriod,
      @Qualifier(value = "rabbitTemplate") RabbitTemplate rabbitTemplate) {
    this.retriever = retriever;
    this.finishLaunchHandler = finishLaunchHandler;
    this.projectExtractor = projectExtractor;
    this.userDetailsService = userDetailsService;
    this.launchFinishGracePeriod = launchFinishGracePeriod;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Override
  public void handleMessage(Message message) {
    Optional<FinishExecutionRQ> finishExecutionRQ = retriever.retrieveValid(message,
        FinishExecutionRQ.class);
    Map<String, Object> headers = message.getMessageProperties().getHeaders();

    finishExecutionRQ.ifPresent(rq -> {

      String username = (String) headers.get(MessageHeaders.USERNAME);
      String projectName = (String) headers.get(MessageHeaders.PROJECT_KEY);
      String launchId = (String) headers.get(MessageHeaders.LAUNCH_ID);
      String baseUrl = (String) headers.get(MessageHeaders.BASE_URL);

      ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
      MembershipDetails membershipDetails = projectExtractor.extractMembershipDetails(user, projectName);

      Optional<Instant> receivedAt = receivedAt(message.getMessageProperties());
      // a message published by an older version has no receive time: its end time was required, so use it as is
      receivedAt.ifPresent(it -> LaunchFinishGracePeriod.applyReceivedTime(rq, it));
      if (receivedAt.isPresent() && deferIfItemsInProgress(message, launchId, membershipDetails,
          receivedAt.get())) {
        return;
      }

      finishLaunchHandler.finishLaunch(launchId, rq, membershipDetails, user, baseUrl);

    });
  }

  private boolean deferIfItemsInProgress(Message message, String launchId, MembershipDetails membershipDetails,
      Instant receivedAt) {
    MessageProperties properties = message.getMessageProperties();
    if (Boolean.TRUE.equals(properties.getHeader(MessageHeaders.LAUNCH_FINISH_DEFERRED))) {
      return false;
    }
    Duration delay = launchFinishGracePeriod.remainingDelay(launchId, membershipDetails.getProjectId(), receivedAt);
    if (delay.isZero()) {
      return false;
    }
    properties.setHeader(MessageHeaders.LAUNCH_FINISH_DEFERRED, true);
    properties.setExpiration(String.valueOf(Math.max(1, delay.toMillis())));
    try {
      rabbitTemplate.invoke(operations -> {
        operations.send("", LAUNCH_FINISH_DELAY_QUEUE, message);
        operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
        return null;
      });
    } catch (RuntimeException e) {
      log.warn("Unable to delay the finish of launch '{}', finishing it now", launchId, e);
      properties.setExpiration(null);
      return false;
    }
    log.debug("Finish of launch '{}' delayed by {} ms to let its items in progress finish", launchId,
        delay.toMillis());
    return true;
  }

  private static Optional<Instant> receivedAt(MessageProperties properties) {
    Object value = properties.getHeader(MessageHeaders.RECEIVED_AT);
    if (value instanceof Number number) {
      return Optional.of(Instant.ofEpochMilli(number.longValue()));
    }
    if (value != null) {
      try {
        return Optional.of(Instant.ofEpochMilli(Long.parseLong(value.toString())));
      } catch (NumberFormatException e) {
        log.warn("Ignoring the malformed '{}' header of a launch finish message: {}", MessageHeaders.RECEIVED_AT,
            value);
      }
    }
    return Optional.empty();
  }
}
