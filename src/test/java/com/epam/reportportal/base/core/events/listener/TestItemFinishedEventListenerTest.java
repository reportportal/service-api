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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.core.events.subscriber.EventSubscriber;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

class TestItemFinishedEventListenerTest {

  @SuppressWarnings("unchecked")
  private final EventSubscriber<TestItemFinishedEvent> subscriber = mock(EventSubscriber.class);

  @SuppressWarnings("unchecked")
  private final EventSubscriber<TestItemFinishedEvent> other = mock(EventSubscriber.class);

  private final TestItemFinishedEvent event = new TestItemFinishedEvent(new TestItem(), 1L);

  @Test
  void eventNoSubscriberAppliesToIsNotQueued() {
    TaskExecutor executor = mock(TaskExecutor.class);
    TestItemFinishedEventListener listener =
        new TestItemFinishedEventListener(List.of(subscriber, other), executor);

    listener.onApplicationEvent(event);

    verify(executor, never()).execute(any());
    verify(subscriber, never()).handleEvent(any());
    verify(other, never()).handleEvent(any());
  }

  @Test
  void eventIsDispatchedToAllSubscribersWhenOneApplies() {
    when(other.isApplicable(event)).thenReturn(true);
    TestItemFinishedEventListener listener =
        new TestItemFinishedEventListener(List.of(subscriber, other), new SyncTaskExecutor());

    listener.onApplicationEvent(event);

    verify(subscriber).handleEvent(event);
    verify(other).handleEvent(event);
  }
}
