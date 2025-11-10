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

package com.epam.reportportal.core.events.handler.project;

import com.epam.reportportal.core.logtype.DefaultLogTypeProvider;
import com.epam.reportportal.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener that creates default log types when a new project is created. Listens to project creation Activity
 * messages from the activity exchange.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectLogTypesInitializer {

  private static final String QUEUE_PROJECT_CREATED = "project.created";
  private static final String ROUTING_KEY_PROJECT_CREATED = "activity.*.PROJECT.createProject";

  private final DefaultLogTypeProvider defaultLogTypeProvider;
  private final LogTypeRepository logTypeRepository;

  /**
   * Handles project creation activity events from RabbitMQ by initializing default log types for the created project.
   *
   * @param activity the activity containing details about the created project.
   */
  @RabbitListener(
      bindings = @QueueBinding(
          value = @Queue(value = QUEUE_PROJECT_CREATED, durable = "true"),
          exchange = @Exchange(value = "activity", type = ExchangeTypes.TOPIC),
          key = ROUTING_KEY_PROJECT_CREATED
      ), containerFactory = "rabbitListenerContainerFactory"
  )
  public void onProjectCreated(@Payload Activity activity) {
    if (Objects.isNull(activity) || Objects.isNull(activity.getProjectId())) {
      log.warn("Activity is missing projectId. Skipping log type initialization.");
      return;
    }

    Long projectId = activity.getProjectId();
    List<ProjectLogType> defaultLogTypes = defaultLogTypeProvider.provideDefaultLogTypes(projectId);
    logTypeRepository.saveAll(defaultLogTypes);

    log.info("Default log types have been initialized for project with ID: {}", projectId);
  }
}
