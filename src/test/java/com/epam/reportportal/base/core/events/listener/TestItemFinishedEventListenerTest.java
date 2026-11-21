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
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.core.events.subscriber.EventSubscriber;
import com.epam.reportportal.base.infrastructure.persistence.entity.ItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

class TestItemFinishedEventListenerTest {

  @SuppressWarnings("unchecked")
  private final EventSubscriber<TestItemFinishedEvent> subscriber = mock(EventSubscriber.class);

  @Test
  void itemWithoutImmediateAnalysisAttributesIsNotDispatched() {
    TaskExecutor executor = mock(TaskExecutor.class);
    TestItemFinishedEventListener listener =
        new TestItemFinishedEventListener(List.of(subscriber), executor);
    TestItem item = new TestItem();
    item.getAttributes().add(new ItemAttribute("os", "linux", false));
    item.getAttributes().add(new ItemAttribute("immediateAutoAnalysis", "false", true));

    listener.onApplicationEvent(new TestItemFinishedEvent(item, 1L));

    verify(executor, never()).execute(any());
    verifyNoInteractions(subscriber);
  }

  @Test
  void itemWithImmediateAutoAnalysisIsDispatched() {
    TestItemFinishedEventListener listener =
        new TestItemFinishedEventListener(List.of(subscriber), new SyncTaskExecutor());
    TestItem item = new TestItem();
    item.getAttributes().add(new ItemAttribute("immediateAutoAnalysis", "true", true));
    TestItemFinishedEvent event = new TestItemFinishedEvent(item, 1L);

    listener.onApplicationEvent(event);

    verify(subscriber).handleEvent(event);
  }

  @Test
  void itemWithImmediatePatternAnalysisIsDispatched() {
    TestItemFinishedEventListener listener =
        new TestItemFinishedEventListener(List.of(subscriber), new SyncTaskExecutor());
    TestItem item = new TestItem();
    item.getAttributes().add(new ItemAttribute("immediatePatternAnalysis", "true", false));
    TestItemFinishedEvent event = new TestItemFinishedEvent(item, 1L);

    listener.onApplicationEvent(event);

    verify(subscriber).handleEvent(event);
  }
}
