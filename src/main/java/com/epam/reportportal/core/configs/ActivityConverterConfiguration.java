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

package com.epam.reportportal.core.configs;

import com.epam.reportportal.core.events.activity.converter.EventToActivityConverter;
import com.epam.reportportal.core.events.domain.AbstractEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for activity converters registry.
 *
 */
@Configuration
public class ActivityConverterConfiguration {

  /**
   * Creates a map of event classes to their corresponding converters. This map is used by
   * ActivityConsumer to look up the appropriate converter for each event type.
   *
   * @param converters List of all EventToActivityConverter beans
   * @return Map of event class to converter
   */
  @Bean
  public Map<Class<? extends AbstractEvent<?>>, EventToActivityConverter<? extends AbstractEvent<?>>> activityConverterMap(
      List<EventToActivityConverter<? extends AbstractEvent<?>>> converters) {
    return converters.stream()
        .collect(Collectors.toMap(
            EventToActivityConverter::getEventClass,
            converter -> converter
        ));
  }
}
