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

package com.epam.ta.reportportal.core.events.subscriber.impl.delegate;

import com.epam.ta.reportportal.core.events.ProjectIdAwareEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.core.events.subscriber.EventSubscriber;
import com.epam.ta.reportportal.core.project.config.ProjectConfigProvider;
import com.epam.ta.reportportal.ws.converter.resource.handler.attribute.launch.LaunchResourceAttributeLogger;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ProjectConfigDelegatingSubscriber<T extends ProjectIdAwareEvent> implements
    EventSubscriber<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConfigDelegatingSubscriber.class);

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
        LOGGER.debug("Error while processing event: " +e.getMessage() );
      }
    });
  }
}
