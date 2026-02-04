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

package com.epam.reportportal.auth.event;

import com.epam.reportportal.base.core.events.domain.UserCreatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.model.activity.UserActivityResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events using Spring's ApplicationEventPublisher. Events will be automatically forwarded to RabbitMQ
 * by DomainEventPublisher after transaction commit.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventPublisher {

  private final ApplicationEventPublisher eventPublisher;

  /**
   * Publishes {@link UserCreatedEvent} when user is created. The event will be automatically forwarded to RabbitMQ by
   * DomainEventPublisher after transaction commit.
   *
   * @param user Created user.
   */
  public void publishOnUserCreated(User user) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(user.getId());
    userActivityResource.setFullName(user.getLogin());

    UserCreatedEvent event = new UserCreatedEvent(userActivityResource);
    eventPublisher.publishEvent(event);
    log.debug("Published domain event: {}", event);
  }
}
