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

import java.util.List;
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
  void preReleaseSortsBelowTheReleaseItPrecedes() {
    assertTrue(PluginVersions.compare("2.0.0-rc1", "2.0.0") < 0);
    assertTrue(PluginVersions.compare("2.0.0", "2.0.0-rc1") > 0);
    // Only against its own numbers: it is still an ancestor of everything below it.
    assertTrue(PluginVersions.compare("2.0.0-rc1", "1.9.9") > 0);
  }

  @Test
  void twoPreReleasesOnTheSameNumbersAreOrderedBySemverRules() {
    assertTrue(PluginVersions.compare("2.0.0-rc1", "2.0.0-rc2") < 0);
    assertEquals(0, PluginVersions.compare("2.0.0-rc1", "2.0.0-rc1"));
  }

  /**
   * The tenth release candidate is above the second. Comparing the suffix as one string put it
   * below, because '1' sorts before '2' — and that is not merely untidy ordering: an update is
   * offered only when the latest version compares above the installed one, so a plugin on
   * {@code rc.10} was read as already ahead of {@code rc.2} and the newer build was never offered.
   */
  @Test
  void numericPreReleaseIdentifierIsComparedAsNumber() {
    assertTrue(PluginVersions.compare("2.0.0-rc.10", "2.0.0-rc.2") > 0);
    assertTrue(PluginVersions.compare("2.0.0-rc.2", "2.0.0-rc.10") < 0);
    assertTrue(PluginVersions.compare("1.0.0-2", "1.0.0-10") < 0);
  }

  @Test
  void numericIdentifierRanksBelowAnAlphanumericOne() {
    // semver 11.4.3: '1.0.0-1' is an earlier stage than '1.0.0-alpha', not a later one
    assertTrue(PluginVersions.compare("1.0.0-1", "1.0.0-alpha") < 0);
    assertTrue(PluginVersions.compare("1.0.0-alpha", "1.0.0-1") > 0);
  }

  @Test
  void shorterRunOfOtherwiseEqualIdentifiersSortsBelowLongerOne() {
    // semver 11.4.4: everything '1.0.0-alpha' says, '1.0.0-alpha.1' says and then some
    assertTrue(PluginVersions.compare("1.0.0-alpha", "1.0.0-alpha.1") < 0);
    assertTrue(PluginVersions.compare("1.0.0-alpha.1", "1.0.0-alpha") > 0);
  }

  @Test
  void theOrderSemverItselfPublishesHolds() {
    // the worked example from the specification, 11.4
    var ascending = List.of("1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-alpha.beta", "1.0.0-beta",
        "1.0.0-beta.2", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0");

    for (var i = 0; i < ascending.size() - 1; i++) {
      var lower = ascending.get(i);
      var higher = ascending.get(i + 1);
      assertTrue(PluginVersions.compare(lower, higher) < 0,
          () -> lower + " should sort below " + higher);
    }
  }

  @Test
  void preReleaseIdentifierTooLargeForTheLongStillOrders() {
    assertTrue(PluginVersions.compare("1.0.0-rc.99999999999999999999", "1.0.0-rc.2") > 0);
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
  void segmentTooLargeForTheLongStillOrders() {
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
