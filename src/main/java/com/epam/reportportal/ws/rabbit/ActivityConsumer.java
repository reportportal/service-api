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

import com.epam.reportportal.core.events.activity.converter.EventToActivityConverter;
import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.epam.reportportal.infrastructure.persistence.dao.ActivityRepository;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes domain events from RabbitMQ, converts them to Activity entities, and persists them.
 * Listens to all domain events via the "domain.#" routing key pattern.
 *
 * @author Andrei Varabyeu
 */
@Component
@Transactional
@RequiredArgsConstructor
public class ActivityConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityConsumer.class);

  private static final String QUEUE_ACTIVITY = "activity";
  private static final String ROUTING_KEY_DOMAIN_ALL = "domain.#";

  private final ActivityRepository activityRepository;
  private final Map<Class<? extends AbstractEvent<?>>, EventToActivityConverter<? extends AbstractEvent<?>>> converterMap;

  @RabbitListener(
      bindings = @QueueBinding(
          value = @Queue(value = QUEUE_ACTIVITY, durable = "true", autoDelete = "false"),
          exchange = @Exchange(value = "domain.events", type = ExchangeTypes.TOPIC),
          key = ROUTING_KEY_DOMAIN_ALL
      ), containerFactory = "rabbitListenerContainerFactory"
  )
  public void onEvent(@Payload AbstractEvent<?> event) {
    Optional.ofNullable(event).ifPresent(this::processEvent);
  }

  private void processEvent(AbstractEvent<?> event) {
    var converter = converterMap.get(event.getClass());

    if (converter == null) {
      LOGGER.debug(
          "No converter found for event type: {}. Event will not be persisted as activity.",
          event.getClass().getSimpleName());
      return;
    }

    Activity activity = convertEvent(event, converter);
    if (activity != null && activity.isSavedEvent()) {
      processActivity(activity);
    }
  }

  @SuppressWarnings("unchecked")
  private <E extends AbstractEvent<?>> Activity convertEvent(E event,
      EventToActivityConverter<? extends AbstractEvent<?>> converter) {
    return ((EventToActivityConverter<E>) converter).convert(event);
  }

  private void processActivity(Activity activity) {
    LOGGER.info("[audit] - {}", activity);
    if (Objects.isNull(activity.getDetails())) {
      activity.setDetails(new ActivityDetails());
    }
    activityRepository.save(activity);
  }

}
