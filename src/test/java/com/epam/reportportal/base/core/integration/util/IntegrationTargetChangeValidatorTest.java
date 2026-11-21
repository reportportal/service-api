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

package com.epam.reportportal.base.core.integration.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IntegrationTargetChangeValidatorTest {

  private static Integration stored(Map<String, Object> params) {
    var integration = new Integration();
    integration.setParams(new IntegrationParams(new HashMap<>(params)));
    return integration;
  }

  @Test
  void hostChangeWithoutSecretIsRejected() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    var ex = assertThrows(ReportPortalException.class,
        () -> IntegrationTargetChangeValidator.validate(integration, Map.of("host", "evil.com")));
    assertEquals(ErrorType.INCORRECT_REQUEST, ex.getErrorType());
  }

  @Test
  void urlChangeWithoutSecretIsRejectedCaseInsensitively() {
    var integration = stored(Map.of("url", "https://jira.old", "apiToken", "cipher"));
    assertThrows(ReportPortalException.class,
        () -> IntegrationTargetChangeValidator.validate(integration, Map.of("URL", "https://evil")));
  }

  @Test
  void targetChangeWithNewSecretIsAllowed() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    assertDoesNotThrow(() -> IntegrationTargetChangeValidator.validate(integration,
        Map.of("host", "smtp.new.com", "password", "new")));
  }

  @Test
  void blankSecretDoesNotCount() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    assertThrows(ReportPortalException.class, () -> IntegrationTargetChangeValidator.validate(integration,
        Map.of("host", "smtp.new.com", "password", " ")));
  }

  @Test
  void unchangedTargetWithoutSecretIsAllowed() {
    var integration = stored(Map.of("host", "smtp.old.com", "port", 25, "password", "cipher"));
    assertDoesNotThrow(() -> IntegrationTargetChangeValidator.validate(integration,
        Map.of("host", "smtp.old.com", "port", "25", "from", "a@b.c")));
  }

  @Test
  void nonTargetChangeWithoutSecretIsAllowed() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    assertDoesNotThrow(() -> IntegrationTargetChangeValidator.validate(integration, Map.of("from", "x@y.z")));
  }

  @Test
  void integrationWithoutStoredSecretIsAllowed() {
    var integration = stored(Map.of("host", "smtp.old.com"));
    assertDoesNotThrow(() -> IntegrationTargetChangeValidator.validate(integration, Map.of("host", "new.com")));
  }
}
