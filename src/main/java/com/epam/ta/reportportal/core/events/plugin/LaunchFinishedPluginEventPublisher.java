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

package com.epam.ta.reportportal.core.events.plugin;

import com.epam.reportportal.extension.event.LaunchFinishedPluginEvent;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Pavel Bortnik</a>
 */
@Service
public class LaunchFinishedPluginEventPublisher implements ConfigurableEventHandler<LaunchFinishedEvent, Map<String, String>> {

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public LaunchFinishedPluginEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void handle(LaunchFinishedEvent event, Map<String, String> config) {
		eventPublisher.publishEvent(new LaunchFinishedPluginEvent(event.getId(), event.getProjectId()));
	}
}
