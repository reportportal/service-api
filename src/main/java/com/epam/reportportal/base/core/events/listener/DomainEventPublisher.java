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

package com.epam.reportportal.base.core.events.listener;

import com.epam.reportportal.base.core.events.MessageBus;
import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes domain events to RabbitMQ after transaction commit. Events are published as-is (no conversion) to the
 * domain.events exchange.
 *
 */
@Slf4j
@Component
public class DomainEventPublisher {

  private static final String DOMAIN_EVENTS_EXCHANGE = "domain.events";

  private final MessageBus messageBus;

  private final TaskExecutor domainEventExecutor;

  public DomainEventPublisher(MessageBus messageBus,
      @Qualifier("domainEventExecutor") TaskExecutor domainEventExecutor) {
    this.messageBus = messageBus;
    this.domainEventExecutor = domainEventExecutor;
  }

  /**
   * Listens to domain events and publishes them to RabbitMQ after transaction commit. Routing key pattern:
   * domain.{objectType}.{action} Local-only events (e.g., high-frequency reporting events) are skipped to avoid
   * unnecessary RabbitMQ traffic; they are filtered before queueing so per-item and per-log events never occupy the
   * executor.
   *
   * @param event The domain event to publish
   */
  @TransactionalEventListener(fallbackExecution = true)
  public void onDomainEvent(AbstractEvent<?> event) {
    if (event.shouldPublishToRabbitMq()) {
      domainEventExecutor.execute(() -> {
        log.debug("Publishing domain event to exchange '{}', event:'{}'", DOMAIN_EVENTS_EXCHANGE,
            event);
        messageBus.publish(DOMAIN_EVENTS_EXCHANGE, generateRoutingKey(event), event);
      });
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
