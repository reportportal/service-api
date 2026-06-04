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

package com.epam.reportportal.base.core.events.subscriber.impl.delegate;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import com.epam.reportportal.base.core.events.handler.ConfigurableEventHandler;
import com.epam.reportportal.base.core.events.subscriber.EventSubscriber;
import com.epam.reportportal.base.core.project.config.ProjectConfigProvider;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscriber that delegates event handling to configured handlers with project-specific configuration. Events must
 * override {@code AbstractEvent#getProjectId()} to provide the project ID.
 *
 * @param <T> Event type that has a projectId
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ProjectConfigDelegatingSubscriber<T extends AbstractEvent<?>> implements
    EventSubscriber<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ProjectConfigDelegatingSubscriber.class);

  private final ProjectConfigProvider projectConfigProvider;
  private final List<ConfigurableEventHandler<T, Map<String, String>>> eventHandlers;

  public ProjectConfigDelegatingSubscriber(ProjectConfigProvider projectConfigProvider,
      List<ConfigurableEventHandler<T, Map<String, String>>> eventHandlers) {
    this.projectConfigProvider = projectConfigProvider;
    this.eventHandlers = eventHandlers;
  }

  @Override
  public void handleEvent(T event) {
    final Map<String, String> projectConfig = projectConfigProvider.provide(event.getProjectId());
    eventHandlers.forEach(h -> {
      try {
        h.handle(event, projectConfig);
      } catch (Exception e) {
        LOGGER.debug("Error while processing event: {}", e.getMessage());
      }
    });
  }
}
