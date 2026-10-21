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

package com.epam.reportportal.base.core.organization.patch;

import com.epam.reportportal.api.model.PatchOperation;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Abstract base class for handling patch operations on organizations. Subclasses should override the adding, replace,
 * and remove methods to implement specific patch logic.
 *
 * @author <a href="mailto:siarhei_hrabko@epam.com">Siarhei Hrabko</a>
 */
@Slf4j
@RequiredArgsConstructor
public class BasePatchOrganizationHandler {

  protected final OrganizationUserService organizationUserService;
  protected final JsonMapper objectMapper;

  void replace(PatchOperation operation, Long orgId) {
    throw new UnsupportedOperationException("'Replace' operation is not supported");
  }

  void add(PatchOperation operation, Long orgId) {
    throw new UnsupportedOperationException("'Add' operation is not supported");
  }

  void remove(PatchOperation operation, Long orgId) {
    throw new UnsupportedOperationException("'Remove' operation is not supported");
  }

  private String valueToString(Object value) {
    return value instanceof String string ? string : objectMapper.writeValueAsString(value);
  }

  /**
   * Reads the value from a PatchOperation and converts it to the specified type using the provided TypeReference.
   *
   * @param operation The patch operation containing the value to read.
   * @param typeRef   The TypeReference indicating the desired type for conversion.
   * @param <T>       The type to which the value should be converted.
   * @return The converted value of the specified type.
   */
  protected <T> T readOperationValue(PatchOperation operation, TypeReference<T> typeRef) {
    try {
      return objectMapper.readValue(valueToString(operation.getValue()), typeRef);
    } catch (JacksonException e) {
      log.error(e.getMessage());
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Invalid field 'value': " + (e.getCause() != null ? e.getCause().getMessage() : e.getOriginalMessage()));
    }
  }
}
