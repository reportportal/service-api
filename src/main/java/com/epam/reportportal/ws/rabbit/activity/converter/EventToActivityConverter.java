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

package com.epam.reportportal.ws.rabbit.activity.converter;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;

/**
 * Converter interface for transforming domain events into Activity entities for audit trail
 * persistence. Each domain event type should have its own converter implementation.
 *
 * @param <E> The specific event type that extends AbstractEvent
 */
public interface EventToActivityConverter<E extends AbstractEvent<?>> {

  /**
   * Converts a domain event to an Activity entity.
   *
   * @param event The domain event to convert
   * @return Activity entity ready for persistence, or null if the event should not be persisted
   */
  Activity convert(E event);

  /**
   * Returns the event class that this converter handles.
   *
   * @return The class of the event type
   */
  Class<E> getEventClass();
}
