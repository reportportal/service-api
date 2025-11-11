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

import com.epam.reportportal.core.organization.PersonalOrganizationService;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer for user created messages.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Shekhtel Reingold</a>
 */
@Component
public class UserCreatedConsumer {

  private final PersonalOrganizationService personalOrganizationService;

  /**
   * Constructor for UserCreatedConsumer.
   *
   * @param personalOrganizationService The service to handle personal organization creation.
   */
  public UserCreatedConsumer(PersonalOrganizationService personalOrganizationService) {
    this.personalOrganizationService = personalOrganizationService;
  }

  /**
   * Handles external user creation events.
   *
   * @param activity The activity payload containing user information.
   */
  @RabbitListener(
      bindings = @QueueBinding(
          value = @Queue(value = "user.created", durable = "true", autoDelete = "false"),
          exchange = @Exchange(value = "activity", type = ExchangeTypes.TOPIC),
          key = "activity.USER.createUser.external"
      ),
      containerFactory = "rabbitListenerContainerFactory"
  )
  public void onEvent(@Payload Activity activity) {
    if (activity != null && activity.getObjectId() != null) {
      personalOrganizationService.createPersonalOrganization(activity.getObjectId());
    }
  }
}
