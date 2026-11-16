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

package com.epam.reportportal.base.ws.controller;

import com.epam.reportportal.base.model.launch.LaunchImportRQ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Part;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Converts, parses, and validates raw JSON strings into {@link LaunchImportRQ} instances for multipart import
 * endpoints.
 */
@Component
@RequiredArgsConstructor
public class LaunchImportRqConverter {

  private final ObjectMapper objectMapper;
  private final Validator validator;

  /**
   * Reads a multipart {@link Part} and parses it into a validated {@link LaunchImportRQ}.
   *
   * @param launchImportRqPart the raw multipart part, or {@code null} if absent
   * @return the parsed and validated {@link LaunchImportRQ}, or {@code null} if absent/blank
   */
  public LaunchImportRQ convert(Part launchImportRqPart) {
    if (launchImportRqPart == null) {
      return null;
    }
    try {
      String launchImportRqJson = new String(launchImportRqPart.getInputStream().readAllBytes(),
          StandardCharsets.UTF_8);
      return convert(launchImportRqJson);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to read launchImportRq part", e);
    }
  }

  private LaunchImportRQ convert(String launchImportRqJson) {
    if (StringUtils.isBlank(launchImportRqJson)) {
      return null;
    }
    try {
      LaunchImportRQ launchImportRq = objectMapper.readValue(launchImportRqJson, LaunchImportRQ.class);
      if (launchImportRq == null) {
        return null;
      }
      Set<ConstraintViolation<LaunchImportRQ>> violations = validator.validate(launchImportRq);
      if (!violations.isEmpty()) {
        throw new ConstraintViolationException(violations);
      }
      return launchImportRq;
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to parse launchImportRq: " + e.getOriginalMessage(), e);
    }
  }
}
