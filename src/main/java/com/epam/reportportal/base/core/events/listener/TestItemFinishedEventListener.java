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

package com.epam.reportportal.base.core.events.listener;

import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.core.events.subscriber.EventSubscriber;
import java.util.List;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Multicast dispatch for test item finished subscribers.
 *
 * <p>Subscribers are asked first whether the item applies to them, and the task is queued only if one does. The
 * executor discards tasks once its queue is full, so items no subscriber can use (most items, since immediate analysis
 * is opted into per item) must not take queue slots from the ones that can.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TestItemFinishedEventListener {

  private final List<EventSubscriber<TestItemFinishedEvent>> subscribers;

  private final TaskExecutor eventListenerExecutor;

  public TestItemFinishedEventListener(List<EventSubscriber<TestItemFinishedEvent>> subscribers,
      TaskExecutor eventListenerExecutor) {
    this.subscribers = subscribers;
    this.eventListenerExecutor = eventListenerExecutor;
  }

  @TransactionalEventListener
  public void onApplicationEvent(TestItemFinishedEvent event) {
    if (subscribers.stream().noneMatch(s -> s.isApplicable(event))) {
      return;
    }
    eventListenerExecutor.execute(() -> subscribers.forEach(s -> s.handleEvent(event)));
  }
}
