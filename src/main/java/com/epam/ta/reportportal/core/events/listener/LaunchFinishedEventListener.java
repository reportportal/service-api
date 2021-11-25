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

import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.subscriber.EventSubscriber;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class LaunchFinishedEventListener {

	private final List<EventSubscriber<LaunchFinishedEvent>> subscribers;

	public LaunchFinishedEventListener(List<EventSubscriber<LaunchFinishedEvent>> subscribers) {
		this.subscribers = subscribers;
	}

	@Async(value = "eventListenerExecutor")
	@TransactionalEventListener
	public void onApplicationEvent(LaunchFinishedEvent event) {
		if (LaunchModeEnum.DEBUG == event.getMode()) {
			return;
		}

		subscribers.forEach(s -> s.handleEvent(event));

	}

}
