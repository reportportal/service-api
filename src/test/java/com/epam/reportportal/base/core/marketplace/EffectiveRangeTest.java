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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import org.junit.jupiter.api.Test;

/**
 * Which range a version is judged by. Every compatibility decision goes through here, so a ceiling
 * that is skipped by mistake would let a capped version install.
 */
class EffectiveRangeTest {

  private static MarketplaceCompatibility range(String value) {
    return new MarketplaceCompatibility(value);
  }

  @Test
  void theEffectiveRangeWinsOverTheDeclaredOne() {
    assertEquals(">=25.1, <27.0", EffectiveRange.of(range(">=25.1, <27.0"), range(">=25.1"), "<27.0"));
  }

  @Test
  void anOldRegistryWithoutAnEffectiveRangeFallsBackToTheDeclaredOne() {
    assertEquals(">=25.1", EffectiveRange.of(null, range(">=25.1"), null));
    assertEquals(">=25.1", EffectiveRange.of(range(null), range(">=25.1"), null));
    assertEquals(">=25.1", EffectiveRange.of(range("  "), range(" >=25.1 "), " "));
  }

  @Test
  void noRangeAnywhereIsUnknown() {
    assertNull(EffectiveRange.of(null, null, null));
    assertNull(EffectiveRange.of(range(""), range(" "), null));
  }

  @Test
  void aCeilingWithoutAnEffectiveRangeIsUnknownRatherThanTheAuthorsRange() {
    // only a ceiling-aware registry sends a ceiling, so a missing effective range is not the old
    // registry fallback
    assertNull(EffectiveRange.of(null, range(">=25.1"), "<27.0"));
    assertNull(EffectiveRange.of(range(" "), range(">=25.1"), "<27.0"));
  }
}
