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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Version ordering. Every compatibility verdict and every "is there an update" verdict is a call
 * into here, so the orderings that a naive string comparison gets wrong are pinned individually.
 */
class PluginVersionsTest {

  @Test
  void segmentsAreComparedNumericallyNotLexically() {
    // "25.10" sorts below "25.9" as text; as a release it is above it.
    assertTrue(PluginVersions.compare("25.10", "25.9") > 0);
    assertTrue(PluginVersions.compare("25.9", "25.10") < 0);
    assertTrue(PluginVersions.compare("1.4.10", "1.4.9") > 0);
  }

  @Test
  void missingSegmentsCountAsZero() {
    assertEquals(0, PluginVersions.compare("1.4", "1.4.0"));
    assertEquals(0, PluginVersions.compare("1.4.0.0", "1.4"));
    assertTrue(PluginVersions.compare("1.4.1", "1.4") > 0);
  }

  @Test
  void leadingZeroesDoNotChangeOrder() {
    assertEquals(0, PluginVersions.compare("1.04", "1.4"));
    assertTrue(PluginVersions.compare("1.010", "1.9") > 0);
  }

  @Test
  void aPreReleaseSortsBelowTheReleaseItPrecedes() {
    assertTrue(PluginVersions.compare("2.0.0-rc1", "2.0.0") < 0);
    assertTrue(PluginVersions.compare("2.0.0", "2.0.0-rc1") > 0);
    // Only against its own numbers: it is still an ancestor of everything below it.
    assertTrue(PluginVersions.compare("2.0.0-rc1", "1.9.9") > 0);
  }

  @Test
  void twoPreReleasesOnTheSameNumbersAreOrderedLexically() {
    assertTrue(PluginVersions.compare("2.0.0-rc1", "2.0.0-rc2") < 0);
    assertEquals(0, PluginVersions.compare("2.0.0-rc1", "2.0.0-rc1"));
  }

  @Test
  void buildMetadataIsIgnoredWhenOrdering() {
    // Semver says build metadata carries no precedence. Ordering on it would put '2.0.0+build'
    // below '2.0.0-rc1', i.e. rank a release candidate above the release itself.
    assertEquals(0, PluginVersions.compare("2.0.0+build.5", "2.0.0"));
    assertEquals(0, PluginVersions.compare("2.0.0-rc1+build.5", "2.0.0-rc1"));
    assertTrue(PluginVersions.compare("2.0.0+build.5", "2.0.0-rc1") > 0);
  }

  @Test
  void aSegmentTooLargeForALongStillOrders() {
    // Twenty digits overflow a long. Parsing one would throw out of the plugins page instead of
    // deciding a version order, and the registry chooses these strings, not us.
    assertTrue(PluginVersions.compare("99999999999999999999", "25.2") > 0);
    assertTrue(PluginVersions.compare("25.2", "99999999999999999999") < 0);
    assertEquals(0,
        PluginVersions.compare("99999999999999999999", "99999999999999999999"));
  }

  @Test
  void nullSortsBelowAnyVersion() {
    assertTrue(PluginVersions.compare(null, "1.0.0") < 0);
    assertTrue(PluginVersions.compare("1.0.0", null) > 0);
    assertEquals(0, PluginVersions.compare(null, null));
  }
}
