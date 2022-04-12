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

import com.epam.ta.reportportal.core.events.activity.item.ItemFinishedEvent;
import com.epam.ta.reportportal.core.events.subscriber.impl.delegate.ProjectConfigDelegatingSubscriber;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class TestItemFinishedEventListenerTest {

	private final ProjectConfigDelegatingSubscriber<ItemFinishedEvent> delegatingSubscriber = (ProjectConfigDelegatingSubscriber<ItemFinishedEvent>) mock(
			ProjectConfigDelegatingSubscriber.class);

	private final TestItemFinishedEventListener eventListener = new TestItemFinishedEventListener(List.of(delegatingSubscriber));

	@Test
	void shouldHandle() {
		final ItemFinishedEvent event = new ItemFinishedEvent(3L, 2L, 1L);

		eventListener.onApplicationEvent(event);

		verify(delegatingSubscriber, times(1)).handleEvent(event);
	}

}