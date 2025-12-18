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

package com.epam.reportportal.ws.rabbit;

import com.epam.reportportal.core.events.domain.UserCreatedEvent;
import com.epam.reportportal.core.organization.PersonalOrganizationService;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer for user created events.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Shekhtel Reingold</a>
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class UserCreatedConsumer {

  private final PersonalOrganizationService personalOrganizationService;

  /**
   * Handles external user creation events.
   *
   * @param event The UserCreatedEvent containing user information
   */
  @RabbitListener(
      bindings = @QueueBinding(
          value = @Queue(value = "user.created", durable = "true", autoDelete = "false"),
          exchange = @Exchange(value = "domain.events", type = ExchangeTypes.TOPIC),
          key = "domain.UserCreatedEvent"
      ), containerFactory = "rabbitListenerContainerFactory"
  )
  public void onEvent(@Payload UserCreatedEvent event) {
    if (Objects.isNull(event) || Objects.isNull(event.getUserActivityResource()) || Objects.isNull(
        event.getUserActivityResource().getId())) {
      log.warn("UserCreatedEvent is missing userId. Personal org initialization.");
      return;
    }

//    todo will be rolled back once org plugin is migrated to the new events structure
//    personalOrganizationService.createPersonalOrganization(event.getUserActivityResource().getId());
  }
}
