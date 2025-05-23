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
import java.util.Arrays;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;


/**
 * A handler for modifying string properties in OpenAPI schemas. This component populates example values for string
 * properties based on their type or annotations.
 */
@Component
public class StringPropertyHandler implements PropertyHandler {

  @Override
  public Schema modifyProperty(Schema property, AnnotatedType annotatedType) {
    populateExample(property, annotatedType);
    return property;
  }

  private void populateExample(Schema property, AnnotatedType annotatedType) {
    if (property.getExample() != null) {
      return;
    }

    if (property.getDefault() != null) {
      property.example(property.getDefault());
    } else if (isUuid(property)) {
      property.example(UUID.randomUUID().toString());
    } else if (isEmail(annotatedType)) {
      property.example(RandomStringUtils.insecure().nextAlphabetic(8) + "@" + "reportportal.com");
    }
  }

  private boolean isEmail(AnnotatedType annotatedType) {
    return Arrays.stream(annotatedType.getCtxAnnotations())
        .anyMatch(annotation -> annotation instanceof jakarta.validation.constraints.Email);
  }

  private boolean isUuid(Schema property) {
    return property.getFormat() != null && property.getFormat().equalsIgnoreCase("uuid");
  }
}
