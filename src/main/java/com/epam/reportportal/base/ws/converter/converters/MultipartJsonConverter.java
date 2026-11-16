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

package com.epam.reportportal.base.ws.converter.converters;

import jakarta.servlet.http.Part;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converts a JSON multipart {@link Part} into a model object regardless of the part's declared content type. Swagger UI
 * sends JSON parts as {@code application/octet-stream}, which Spring's {@code @RequestPart} cannot deserialize into a
 * model on its own.
 */
@Component
@RequiredArgsConstructor
public class MultipartJsonConverter {

  private final JsonMapper jsonMapper;
  private final Validator validator;

  /**
   * Reads a multipart {@link Part} and parses it into the given type. A single JSON object is accepted where an array
   * is expected.
   *
   * <p>Non-array results are validated with bean validation. Array elements are not validated here, so callers can
   * validate and report them item by item.
   *
   * @param part the raw multipart part, or {@code null} if absent
   * @param type the target type, a model class or an array of model classes
   * @param <T>  the target type
   * @return the parsed object; {@code null} if the part is absent or blank (an empty array for array types)
   * @throws ConstraintViolationException if bean validation fails
   * @throws IllegalArgumentException     if JSON parsing fails
   */
  public <T> T convert(Part part, Class<T> type) {
    if (part == null) {
      return null;
    }
    String json = readContent(part);
    if (StringUtils.isBlank(json)) {
      return type.isArray() ? type.cast(Array.newInstance(type.getComponentType(), 0)) : null;
    }
    T value = parse(json, type);
    if (value != null && !type.isArray()) {
      validate(value);
    }
    return value;
  }

  private String readContent(Part part) {
    try {
      return new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to read multipart part '" + part.getName() + "'", e);
    }
  }

  private <T> T parse(String json, Class<T> type) {
    try {
      return jsonMapper.readerFor(type)
          .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
          .readValue(json);
    } catch (JacksonException e) {
      throw new IllegalArgumentException(
          "Unable to parse " + type.getSimpleName() + ": " + e.getOriginalMessage(), e);
    }
  }

  private <T> void validate(T value) {
    Set<ConstraintViolation<T>> violations = validator.validate(value);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
