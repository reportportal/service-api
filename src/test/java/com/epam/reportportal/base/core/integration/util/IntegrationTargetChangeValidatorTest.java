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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.List;
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

  /**
   * Runs the validator and then the same shallow merge as {@code BasicIntegrationServiceImpl}.
   */
  private static Map<String, Object> validateAndMerge(Integration integration, Map<String, Object> request) {
    IntegrationTargetChangeValidator.validate(integration, request);
    integration.getParams().getParams().putAll(request);
    return integration.getParams().getParams();
  }

  @Test
  void targetChangeWithNewSecretIsAllowedAndKeepsOnlyTheNewSecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    var result = validateAndMerge(integration, Map.of("host", "smtp.new.com", "password", "new"));
    assertEquals("new", result.get("password"));
    assertEquals("smtp.new.com", result.get("host"));
  }

  @Test
  void targetChangeWithAnotherSecretKeyDropsTheStoredSecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "username", "u", "password", "cipher"));
    var result = validateAndMerge(integration, Map.of("host", "smtp.attacker.tld", "apiToken", "x"));
    assertFalse(result.containsKey("password"));
    assertFalse(result.containsValue("cipher"));
    assertEquals("u", result.get("username"));
  }

  @Test
  void targetChangeDropsEveryStoredSecretNotResupplied() {
    var integration = stored(Map.of("url", "https://bts.old", "password", "c1", "apiToken", "c2",
        "oauthAccessKey", "c3"));
    var result = validateAndMerge(integration, Map.of("url", "https://bts.new", "apiToken", "t-new"));
    assertEquals("t-new", result.get("apiToken"));
    assertFalse(result.containsKey("password"));
    assertFalse(result.containsKey("oauthAccessKey"));
  }

  @Test
  void blankSecretClearsTheStoredSecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    var result = validateAndMerge(integration, Map.of("host", "smtp.new.com", "password", " "));
    assertEquals(" ", result.get("password"));
    assertFalse(result.containsValue("cipher"));
  }

  @Test
  void authSwitchedOffWithNewHostLeavesNoStoredSecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "authEnabled", true, "username", "u",
        "password", "cipher"));
    var request = new HashMap<String, Object>();
    request.put("host", "smtp.new.com");
    request.put("authEnabled", false);
    request.put("username", "");
    request.put("password", "");
    var result = validateAndMerge(integration, request);
    assertFalse(result.containsValue("cipher"));
  }

  @Test
  void secretKeysAreMatchedCaseInsensitively() {
    var integration = stored(Map.of("host", "smtp.old.com", "Password", "cipher"));
    var result = validateAndMerge(integration, Map.of("host", "smtp.new.com", "APITOKEN", "x"));
    assertFalse(result.containsKey("Password"));
  }

  @Test
  void nestedStoredSecretIsDroppedOnTargetChange() {
    var integration = stored(Map.of("host", "smtp.old.com",
        "auth", new HashMap<>(Map.of("username", "u", "password", "cipher")),
        "servers", List.of(Map.of("name", "a", "apiKey", "cipher2"))));
    var result = validateAndMerge(integration, Map.of("host", "smtp.new.com", "apiToken", "x"));
    assertEquals(Map.of("username", "u"), result.get("auth"));
    assertEquals(List.of(Map.of("name", "a")), result.get("servers"));
  }

  @Test
  void nestedTargetChangeIsDetected() {
    var integration = stored(Map.of("connection", Map.of("url", "https://bts.old"), "password", "cipher"));
    assertThrows(ReportPortalException.class, () -> IntegrationTargetChangeValidator.validate(integration,
        Map.of("connection", Map.of("url", "https://evil"))));
    var result = validateAndMerge(integration,
        Map.of("connection", Map.of("url", "https://evil"), "settings", Map.of("apiToken", "x")));
    assertFalse(result.containsKey("password"));
  }

  @Test
  void nestedSecretInRequestCountsAsNamingASecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    var result = validateAndMerge(integration,
        Map.of("host", "smtp.new.com", "auth", Map.of("password", "new")));
    assertFalse(result.containsKey("password"));
    assertEquals(Map.of("password", "new"), result.get("auth"));
  }

  @Test
  void protocolChangeIsATargetChange() {
    var integration = stored(Map.of("protocol", "smtp", "password", "cipher"));
    var ex = assertThrows(ReportPortalException.class,
        () -> IntegrationTargetChangeValidator.validate(integration, Map.of("protocol", "smtps")));
    assertTrue(ex.getMessage().contains("protocol"));
  }

  @Test
  void unchangedTargetWithoutSecretKeepsTheStoredSecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "port", 25, "password", "cipher"));
    var result = validateAndMerge(integration, Map.of("host", "smtp.old.com", "port", "25", "from", "a@b.c"));
    assertEquals("cipher", result.get("password"));
  }

  @Test
  void nonTargetChangeWithoutSecretKeepsTheStoredSecret() {
    var integration = stored(Map.of("host", "smtp.old.com", "password", "cipher"));
    var result = validateAndMerge(integration, Map.of("from", "x@y.z"));
    assertEquals("cipher", result.get("password"));
  }

  @Test
  void integrationWithoutStoredSecretIsAllowed() {
    var integration = stored(Map.of("host", "smtp.old.com"));
    assertDoesNotThrow(() -> IntegrationTargetChangeValidator.validate(integration, Map.of("host", "new.com")));
  }
}
