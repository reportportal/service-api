/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.base.core.events.subscriber.impl.delegate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import com.epam.reportportal.base.core.events.domain.item.IssueResolvedEvent;
import com.epam.reportportal.base.core.events.handler.ConfigurableEventHandler;
import com.epam.reportportal.base.core.project.config.ProjectConfigProvider;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ProjectConfigDelegatingSubscriberTest {

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    logger = (Logger) LoggerFactory.getLogger(ProjectConfigDelegatingSubscriber.class);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(appender);
  }

  @Test
  @SuppressWarnings("unchecked")
  void handlerFailureIsLoggedAtWarnWithExceptionAndOtherHandlersStillRun() {
    ProjectConfigProvider provider = mock(ProjectConfigProvider.class);
    when(provider.provide(1L)).thenReturn(Map.of());
    ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> failing =
        mock(ConfigurableEventHandler.class);
    ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> next =
        mock(ConfigurableEventHandler.class);
    when(failing.isApplicable(any())).thenReturn(true);
    when(next.isApplicable(any())).thenReturn(true);
    RuntimeException failure = new RuntimeException("analyzer is down");
    doThrow(failure).when(failing).handle(any(), any());
    IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);

    new ProjectConfigDelegatingSubscriber<>(provider, List.of(failing, next)).handleEvent(event);

    verify(next).handle(event, Map.of());
    assertEquals(1, appender.list.size());
    ILoggingEvent logged = appender.list.get(0);
    assertEquals(Level.WARN, logged.getLevel());
    assertSame(failure, ((ThrowableProxy) logged.getThrowableProxy()).getThrowable());
  }

  @Test
  @SuppressWarnings("unchecked")
  void projectConfigIsNotLoadedWhenNoHandlerApplies() {
    ProjectConfigProvider provider = mock(ProjectConfigProvider.class);
    ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> first =
        mock(ConfigurableEventHandler.class);
    ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> second =
        mock(ConfigurableEventHandler.class);
    IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);
    ProjectConfigDelegatingSubscriber<IssueResolvedEvent> subscriber =
        new ProjectConfigDelegatingSubscriber<>(provider, List.of(first, second));

    assertFalse(subscriber.isApplicable(event));
    subscriber.handleEvent(event);

    verifyNoInteractions(provider);
    verify(first, never()).handle(any(), any());
    verify(second, never()).handle(any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void onlyApplicableHandlersRunAndConfigIsLoadedOnce() {
    ProjectConfigProvider provider = mock(ProjectConfigProvider.class);
    Map<String, String> config = Map.of("key", "value");
    when(provider.provide(1L)).thenReturn(config);
    ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> skipped =
        mock(ConfigurableEventHandler.class);
    ConfigurableEventHandler<IssueResolvedEvent, Map<String, String>> applicable =
        mock(ConfigurableEventHandler.class);
    IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);
    when(applicable.isApplicable(event)).thenReturn(true);
    ProjectConfigDelegatingSubscriber<IssueResolvedEvent> subscriber =
        new ProjectConfigDelegatingSubscriber<>(provider, List.of(skipped, applicable));

    assertTrue(subscriber.isApplicable(event));
    subscriber.handleEvent(event);

    verify(provider).provide(1L);
    verify(applicable).handle(event, config);
    verify(skipped, never()).handle(any(), any());
  }
}
