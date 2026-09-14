/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.ws.rabbit;

import com.epam.reportportal.auth.event.SamlProvidersReloadEvent;
import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationCreatedEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationDeletedEvent;
import com.epam.reportportal.base.core.events.domain.IntegrationUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.model.activity.IntegrationActivityResource;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reacts to {@link IntegrationCreatedEvent}, {@link IntegrationUpdatedEvent} and {@link IntegrationDeletedEvent}
 * broadcasts on the {@code domain.events} exchange (published by {@code DomainEventPublisher} on every service-api
 * instance) and, when the changed integration is of the "saml" auth type, republishes {@link SamlProvidersReloadEvent}
 * on this instance's local event bus.
 *
 * <p>plugin-auth-saml already registers a listener for {@link SamlProvidersReloadEvent} directly on the host's
 * {@code ApplicationEventMulticaster} (its beans live in the same JVM/context, just outside component-scan), so
 * republishing it here is enough to make that listener reload its SAML relying party registrations - including on
 * instances that did not handle the original create/update/delete request. The instance that did handle the request
 * also reloads directly (see {@code SamlIntegrationStrategy.save()} / {@code DeleteIntegrationHandlerImpl}); receiving
 * this broadcast back is a harmless, idempotent extra reload.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthIntegrationChangedConsumer {

  private static final String QUEUE_AUTH_INTEGRATION_CHANGED = "auth.integration.changed";
  private static final String SAML_TYPE_NAME = "saml";

  private final IntegrationTypeRepository integrationTypeRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Handles integration create/update/delete broadcasts and triggers a local SAML reload when relevant.
   *
   * @param event The integration domain event received from RabbitMQ
   */
  @RabbitListener(
      bindings = @QueueBinding(
          value = @Queue(value = QUEUE_AUTH_INTEGRATION_CHANGED, durable = "true", autoDelete = "false"),
          exchange = @Exchange(value = "domain.events", type = ExchangeTypes.TOPIC),
          key = {"domain.IntegrationCreatedEvent", "domain.IntegrationUpdatedEvent", "domain.IntegrationDeletedEvent"}
      ), containerFactory = "rabbitListenerContainerFactory", admin = "amqpAdmin"
  )
  public void onEvent(@Payload AbstractEvent<?> event) {
    resolveTypeName(event)
        .filter(SAML_TYPE_NAME::equalsIgnoreCase)
        .ifPresent(typeName -> integrationTypeRepository.findByName(SAML_TYPE_NAME)
            .ifPresentOrElse(
                type -> {
                  log.debug("Reloading SAML relying parties in reaction to {}", event.getClass().getSimpleName());
                  eventPublisher.publishEvent(new SamlProvidersReloadEvent(type));
                },
                () -> log.warn(
                    "Received a SAML integration change broadcast but no '{}' integration type is registered "
                        + "on this instance; skipping relying party reload", SAML_TYPE_NAME)));
  }

  private Optional<String> resolveTypeName(AbstractEvent<?> event) {
    if (event instanceof IntegrationCreatedEvent created) {
      return Optional.ofNullable(created.getIntegrationActivityResource())
          .map(IntegrationActivityResource::getTypeName);
    }
    if (event instanceof IntegrationUpdatedEvent updated) {
      return Optional.ofNullable(updated.getAfter()).map(IntegrationActivityResource::getTypeName);
    }
    if (event instanceof IntegrationDeletedEvent deleted) {
      return Optional.ofNullable(deleted.getIntegrationActivityResource())
          .map(IntegrationActivityResource::getTypeName);
    }
    return Optional.empty();
  }
}
