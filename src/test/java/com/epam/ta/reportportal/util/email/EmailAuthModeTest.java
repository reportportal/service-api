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

package com.epam.ta.reportportal.util.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * @author ReportPortal
 */
class EmailAuthModeTest {

  @Test
  void findByNameIsCaseInsensitive() {
    assertEquals(EmailAuthMode.OAUTH2, EmailAuthMode.findByName("oauth2").orElseThrow());
    assertEquals(EmailAuthMode.BASIC, EmailAuthMode.findByName("Basic").orElseThrow());
    assertFalse(EmailAuthMode.findByName("noSuchMode").isPresent());
    assertFalse(EmailAuthMode.findByName(null).isPresent());
  }

  @Test
  void resolveUsesAuthModeWhenPresent() {
    Map<String, Object> params = Map.of(
        OAuth2EmailSettings.AUTH_MODE.getAttribute(), "OAUTH2",
        EmailSettingsEnum.AUTH_ENABLED.getAttribute(), false
    );

    assertEquals(EmailAuthMode.OAUTH2, EmailAuthMode.resolve(params));
  }

  /**
   * Every Email Server integration persisted before this enum existed only has the legacy boolean
   * {@code authEnabled} flag. It must keep resolving to exactly the mode it behaved as before.
   */
  @Test
  void resolveFallsBackToLegacyAuthEnabledWhenAuthModeAbsent() {
    assertEquals(EmailAuthMode.BASIC,
        EmailAuthMode.resolve(Map.of(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), true)));
    assertEquals(EmailAuthMode.BASIC,
        EmailAuthMode.resolve(Map.of(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), "true")));
    assertEquals(EmailAuthMode.OFF,
        EmailAuthMode.resolve(Map.of(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), false)));
  }

  @Test
  void resolveDefaultsToOffWhenNeitherKeyIsPresent() {
    assertEquals(EmailAuthMode.OFF, EmailAuthMode.resolve(Map.of()));
  }

  @Test
  void allValuesAreCoveredByFindByName() {
    for (EmailAuthMode mode : EmailAuthMode.values()) {
      assertTrue(EmailAuthMode.findByName(mode.name()).isPresent());
    }
  }

}
