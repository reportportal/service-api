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

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.media.Schema;

/**
 * Interface for handling and modifying OpenAPI schema properties. Implementations of this interface provide logic to
 * modify schema properties based on their type or annotations.
 */
public interface PropertyHandler {

  /**
   * Modifies the given schema property using the provided annotated type.
   *
   * @param property      The schema property to modify.
   * @param annotatedType The annotated type providing additional context for modification.
   * @return The modified schema property.
   */
  Schema modifyProperty(Schema property, AnnotatedType annotatedType);
}
