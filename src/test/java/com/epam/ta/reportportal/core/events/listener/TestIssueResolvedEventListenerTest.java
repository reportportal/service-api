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

package com.epam.ta.reportportal.core.events.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.ta.reportportal.core.events.activity.item.IssueResolvedEvent;
import com.epam.ta.reportportal.core.events.subscriber.impl.delegate.ProjectConfigDelegatingSubscriber;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class TestIssueResolvedEventListenerTest {

  private final ProjectConfigDelegatingSubscriber<IssueResolvedEvent> delegatingSubscriber = (ProjectConfigDelegatingSubscriber<IssueResolvedEvent>) mock(
      ProjectConfigDelegatingSubscriber.class);

  private final TestItemIssueResolvedEventListener eventListener = new TestItemIssueResolvedEventListener(
      List.of(delegatingSubscriber));

  @Test
  void shouldHandle() {
    final IssueResolvedEvent event = new IssueResolvedEvent(3L, 2L, 1L);

    eventListener.onApplicationEvent(event);

    verify(delegatingSubscriber, times(1)).handleEvent(event);
  }

}