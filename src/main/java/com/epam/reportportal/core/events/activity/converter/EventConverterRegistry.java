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

package com.epam.reportportal.core.events.activity.converter;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Registry for event-to-activity converters. Encapsulates converter lookup logic and provides
 * type-safe access to converters.
 *
 */
@Component
@RequiredArgsConstructor
public class EventConverterRegistry {

  private final Map<Class<? extends AbstractEvent<?>>,
      EventToActivityConverter<? extends AbstractEvent<?>>> converters;

  @Autowired
  public EventConverterRegistry(
      List<EventToActivityConverter<? extends AbstractEvent<?>>> converterList) {
    this.converters = converterList.stream()
        .collect(Collectors.toUnmodifiableMap(
            EventToActivityConverter::getEventClass,
            Function.identity()
        ));
  }

  /**
   * Convenience method to convert an event to an Activity. Returns empty Optional if no converter
   * is registered for the event type.
   *
   * @param event The event to convert
   * @return Optional containing the Activity if conversion is successful, empty otherwise
   */
  @SuppressWarnings("unchecked")
  public Optional<Activity> convert(AbstractEvent<?> event) {
    return Optional.ofNullable(converters.get(event.getClass()))
        .map(converter -> ((EventToActivityConverter<AbstractEvent<?>>) converter)
            .convert(event));
  }
}
