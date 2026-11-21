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

package com.epam.reportportal.base.core.events.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.reportportal.base.core.events.MessageBus;
import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

class DomainEventPublisherTest {

  private final MessageBus messageBus = mock(MessageBus.class);

  @Test
  void localOnlyEventIsNotQueued() {
    TaskExecutor executor = mock(TaskExecutor.class);
    DomainEventPublisher publisher = new DomainEventPublisher(messageBus, executor);

    publisher.onDomainEvent(new TestItemFinishedEvent(new TestItem(), 1L));

    verify(executor, never()).execute(any());
    verifyNoInteractions(messageBus);
  }

  @Test
  void domainEventIsPublishedThroughExecutor() {
    DomainEventPublisher publisher = new DomainEventPublisher(messageBus, new SyncTaskExecutor());
    LaunchFinishedEvent event = new LaunchFinishedEvent();

    publisher.onDomainEvent(event);

    verify(messageBus).publish(eq("domain.events"), eq("domain.LaunchFinishedEvent"), eq(event));
  }
}
