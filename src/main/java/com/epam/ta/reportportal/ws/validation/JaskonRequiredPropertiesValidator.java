/*
 * Copyright 2019 EPAM Systems
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


package com.epam.ta.reportportal.ws.validation;

import com.epam.ta.reportportal.commons.accessible.Accessible;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class JaskonRequiredPropertiesValidator implements Validator {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      JaskonRequiredPropertiesValidator.class);

  @Override
  public boolean supports(Class<?> clazz) {
    return AnnotationUtils.isAnnotationDeclaredLocally(JsonInclude.class, clazz);
  }

  @Override
  public void validate(Object object, Errors errors) {
    for (Field field : collectFields(object.getClass())) {
      if (AnnotationUtils.isAnnotationDeclaredLocally(JsonInclude.class, field.getType())) {
        try {
          Object innerObject = Accessible.on(object).field(field).getValue();
          if (null != innerObject) {
            errors.pushNestedPath(field.getName());
            validate(innerObject, errors);
          }
        } catch (Exception e) {
          LOGGER.error("JaskonRequiredPropertiesValidator error: " + e.getMessage(), e);
          // do nothing
        }

      }
      if (field.isAnnotationPresent(JsonProperty.class) && field.getAnnotation(JsonProperty.class)
          .required()) {
        String errorCode = "NotNull." + field.getName();
        ValidationUtils.rejectIfEmpty(errors, field.getName(), errorCode, new Object[]{errorCode});
      }
    }
    if (errors.getNestedPath() != null && errors.getNestedPath().length() != 0) {
      errors.popNestedPath();
    }
  }

  private List<Field> collectFields(Class<?> clazz) {
    List<Field> fields = null;
    if (!Object.class.equals(clazz.getSuperclass())) {
      fields = collectFields(clazz.getSuperclass());
    }

    fields = (fields == null) ? new ArrayList<>() : fields;
    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
    return fields;
  }
}