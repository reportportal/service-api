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

package com.epam.reportportal.core.events.listener;

import com.epam.reportportal.core.events.MessageBus;
import com.epam.reportportal.core.events.domain.AbstractEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes domain events to RabbitMQ after transaction commit. Events are published as-is (no
 * conversion) to the domain.events exchange.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

  private static final String DOMAIN_EVENTS_EXCHANGE = "domain.events";

  private final MessageBus messageBus;

  /**
   * Listens to domain events and publishes them to RabbitMQ after transaction commit. Routing key
   * pattern: domain.{objectType}.{action} Local-only events (e.g., high-frequency reporting events)
   * are skipped to avoid unnecessary RabbitMQ traffic.
   *
   * @param event The domain event to publish
   */
  @Async(value = "eventListenerExecutor")
  @TransactionalEventListener
  public void onDomainEvent(AbstractEvent<?> event) {
    if (event.shouldPublishToRabbitMQ()) {
      log.debug("Publishing domain event to exchange '{}', event:'{}'", DOMAIN_EVENTS_EXCHANGE,
          event.toString());
      messageBus.publish(DOMAIN_EVENTS_EXCHANGE, generateRoutingKey(event), event);
    }
  }

  /**
   * Generates routing key for domain events. Pattern: domain.{EventClassName}
   *
   * @param event The domain event
   * @return Routing key
   */
  private String generateRoutingKey(AbstractEvent<?> event) {
    return String.format("domain.%s", event.getClass().getSimpleName());
  }
}
