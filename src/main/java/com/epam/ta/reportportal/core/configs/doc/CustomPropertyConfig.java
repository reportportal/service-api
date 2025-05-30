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

package com.epam.ta.reportportal.core.configs.doc;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for defining custom property handlers for OpenAPI schemas. This class sets up a strategy for
 * handling different property types (e.g., "string", "integer").
 */
@Configuration
public class CustomPropertyConfig {

  private final IntegerPropertyHandler integerPropertyHandler;
  private final StringPropertyHandler stringPropertyHandler;

  /**
   * Constructs a new instance of {@link CustomPropertyConfig}.
   *
   * @param integerPropertyHandler The handler for integer properties.
   * @param stringPropertyHandler  The handler for string properties.
   */
  @Autowired
  public CustomPropertyConfig(IntegerPropertyHandler integerPropertyHandler,
      StringPropertyHandler stringPropertyHandler) {
    this.integerPropertyHandler = integerPropertyHandler;
    this.stringPropertyHandler = stringPropertyHandler;
  }

  /**
   * Defines a bean for the property handler strategy. This strategy maps property types (e.g., "string", "integer") to
   * their corresponding handlers.
   *
   * @return A map of property types to their respective {@link PropertyHandler} implementations.
   */
  @Bean
  public Map<String, PropertyHandler> propertyHandlerStrategy() {
    Map<String, PropertyHandler> map = new HashMap<>();
    map.put("string", stringPropertyHandler);
    map.put("integer", integerPropertyHandler);
    return map;
  }

}
