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

import static com.epam.reportportal.base.core.events.handler.item.TestItemAutoAnalysisRunner.IMMEDIATE_AUTO_ANALYSIS;
import static com.epam.reportportal.base.core.events.handler.item.TestItemPatternAnalysisRunner.IMMEDIATE_PATTERN_ANALYSIS;

import com.epam.reportportal.base.core.events.domain.item.TestItemFinishedEvent;
import com.epam.reportportal.base.core.events.subscriber.EventSubscriber;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Multicast dispatch for test item finished subscribers.
 *
 * <p>The only subscribers run immediate auto/pattern analysis, which needs an
 * {@code immediateAutoAnalysis} or {@code immediatePatternAnalysis} attribute on the item. Items without them are
 * filtered here, before the task is queued, so the per-item project config lookup and the executor slot are spent only
 * on items that can use them.
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
    if (!isImmediateAnalysisRequested(event.getTestItem())) {
      return;
    }
    eventListenerExecutor.execute(() -> subscribers.forEach(s -> s.handleEvent(event)));
  }

  static boolean isImmediateAnalysisRequested(TestItem testItem) {
    return testItem.getAttributes().stream().anyMatch(
        attribute -> (IMMEDIATE_AUTO_ANALYSIS.equals(attribute.getKey())
            || IMMEDIATE_PATTERN_ANALYSIS.equals(attribute.getKey()))
            && Boolean.parseBoolean(attribute.getValue()));
  }
}
