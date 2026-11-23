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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitOperations.OperationsCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LaunchFinishMessageHandlerTest {

  private static final String LAUNCH_UUID = "launch-uuid";
  private static final long PROJECT_ID = 7L;

  @Mock
  private MessageRetriever retriever;
  @Mock
  private FinishLaunchHandler finishLaunchHandler;
  @Mock
  private ProjectExtractor projectExtractor;
  @Mock
  private DefaultUserDetailsService userDetailsService;
  @Mock
  private LaunchFinishGracePeriod gracePeriod;
  @Mock
  private RabbitTemplate rabbitTemplate;
  @Mock
  private RabbitOperations operations;
  @Mock
  private ReportPortalUser user;
  @Mock
  private MembershipDetails membershipDetails;

  private LaunchFinishMessageHandler handler;
  private FinishExecutionRQ request;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    handler = new LaunchFinishMessageHandler(retriever, finishLaunchHandler, projectExtractor, userDetailsService,
        gracePeriod, rabbitTemplate);
    request = new FinishExecutionRQ();
    when(retriever.retrieveValid(any(), eq(FinishExecutionRQ.class))).thenReturn(Optional.of(request));
    when(userDetailsService.loadUserByUsername("user")).thenReturn(user);
    when(projectExtractor.extractMembershipDetails(user, "project")).thenReturn(membershipDetails);
    when(membershipDetails.getProjectId()).thenReturn(PROJECT_ID);
    when(rabbitTemplate.invoke(any(OperationsCallback.class))).thenAnswer(
        invocation -> ((OperationsCallback<Object>) invocation.getArgument(0)).doInRabbit(operations));
  }

  @Test
  void finishWithItemsInProgressIsMovedToTheDelayQueueForTheRestOfTheGracePeriod() {
    Instant receivedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    when(gracePeriod.remainingDelay(LAUNCH_UUID, PROJECT_ID, receivedAt)).thenReturn(Duration.ofMillis(2_400));
    Message message = message(receivedAt.toEpochMilli());

    handler.handleMessage(message);

    verify(finishLaunchHandler, never()).finishLaunch(any(), any(), any(), any(), any());
    ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
    verify(operations).send(eq(""), eq(LAUNCH_FINISH_DELAY_QUEUE), sent.capture());
    verify(operations).waitForConfirmsOrDie(anyLong());
    MessageProperties properties = sent.getValue().getMessageProperties();
    assertEquals("2400", properties.getExpiration());
    assertEquals(true, properties.getHeader(MessageHeaders.LAUNCH_FINISH_DEFERRED));
    assertEquals(LAUNCH_UUID, properties.getHeader(MessageHeaders.HASH_ON), "keeps the routing to the launch queue");
  }

  @Test
  void deferredFinishIsProcessedWithTheReceiveTimeAsEndTime() {
    Instant receivedAt = Instant.parse("2026-11-22T10:00:00.123Z");
    Message message = message(receivedAt.toEpochMilli());
    message.getMessageProperties().setHeader(MessageHeaders.LAUNCH_FINISH_DEFERRED, true);

    handler.handleMessage(message);

    verifyNoInteractions(gracePeriod, rabbitTemplate);
    verify(finishLaunchHandler).finishLaunch(LAUNCH_UUID, request, membershipDetails, user, "http://base");
    assertEquals(receivedAt, request.getEndTime());
  }

  @Test
  void endTimeOfTheRequestIsKept() {
    Instant endTime = Instant.parse("2026-11-22T09:59:59Z");
    request.setEndTime(endTime);
    Instant receivedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    when(gracePeriod.remainingDelay(LAUNCH_UUID, PROJECT_ID, receivedAt)).thenReturn(Duration.ZERO);

    handler.handleMessage(message(receivedAt.toEpochMilli()));

    verify(finishLaunchHandler).finishLaunch(LAUNCH_UUID, request, membershipDetails, user, "http://base");
    assertEquals(endTime, request.getEndTime());
    verifyNoInteractions(rabbitTemplate);
  }

  @Test
  void messageOfAnOlderVersionWithoutReceiveTimeIsNotDelayed() {
    request.setEndTime(Instant.now());

    handler.handleMessage(message(null));

    verifyNoInteractions(gracePeriod, rabbitTemplate);
    verify(finishLaunchHandler).finishLaunch(LAUNCH_UUID, request, membershipDetails, user, "http://base");
  }

  @Test
  void launchIsFinishedAtOnceWhenTheDelayCannotBePublished() {
    Instant receivedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    when(gracePeriod.remainingDelay(LAUNCH_UUID, PROJECT_ID, receivedAt)).thenReturn(Duration.ofSeconds(1));
    doThrow(new AmqpIOException(new java.io.IOException("broken"))).when(operations)
        .send(any(String.class), any(String.class), any(Message.class));
    Message message = message(receivedAt.toEpochMilli());

    handler.handleMessage(message);

    verify(finishLaunchHandler).finishLaunch(LAUNCH_UUID, request, membershipDetails, user, "http://base");
    assertNull(message.getMessageProperties().getExpiration());
  }

  private static Message message(Long receivedAt) {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.HASH_ON, LAUNCH_UUID);
    properties.setHeader(MessageHeaders.USERNAME, "user");
    properties.setHeader(MessageHeaders.PROJECT_KEY, "project");
    properties.setHeader(MessageHeaders.LAUNCH_ID, LAUNCH_UUID);
    properties.setHeader(MessageHeaders.BASE_URL, "http://base");
    if (receivedAt != null) {
      properties.setHeader(MessageHeaders.RECEIVED_AT, receivedAt);
    }
    return new Message("{}".getBytes(), properties);
  }
}
