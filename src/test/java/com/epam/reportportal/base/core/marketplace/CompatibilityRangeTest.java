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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The compatibility range a plugin version declares. This is the guarantee that stops an upgrade
 * being offered that will not run here, so every operator arm and both ends of a window are pinned.
 */
class CompatibilityRangeTest {

  private static boolean matches(String range, String productVersion) {
    return CompatibilityRange.parse(range)
        .orElseThrow(() -> new AssertionError("range '" + range + "' did not parse"))
        .matches(productVersion);
  }

  private static boolean parses(String range) {
    return CompatibilityRange.parse(range).isPresent();
  }

  @Test
  void inclusiveLowerBoundAdmitsItsOwnVersionAndRejectsTheOneBelow() {
    assertTrue(matches(">=25.1", "25.1"));
    assertTrue(matches(">=25.1", "25.2"));
    assertFalse(matches(">=25.1", "25.0"));
  }

  @Test
  void strictLowerBoundRejectsItsOwnVersion() {
    assertFalse(matches(">25.1", "25.1"));
    assertTrue(matches(">25.1", "25.1.1"));
    assertFalse(matches(">25.1", "25.0"));
  }

  @Test
  void strictUpperBoundRejectsItsOwnVersionAndEverythingAbove() {
    assertTrue(matches("<26.0", "25.9"));
    assertFalse(matches("<26.0", "26.0"));
    assertFalse(matches("<26.0", "26.1"));
  }

  @Test
  void inclusiveUpperBoundAdmitsItsOwnVersion() {
    assertTrue(matches("<=25.1", "25.1"));
    assertTrue(matches("<=25.1", "25.0"));
    assertFalse(matches("<=25.1", "25.1.1"));
  }

  @Test
  void aReleaseAboveTheUpperBoundOfAWindowIsIncompatible() {
    // Both ends of the flagship window must bite, or the upper bound is decoration.
    assertTrue(matches(">=25.1, <26.0", "25.2"));
    assertFalse(matches(">=25.1, <26.0", "25.0"));
    assertFalse(matches(">=25.1, <26.0", "26.0"));
    assertFalse(matches(">=25.1, <26.0", "27.3"));
  }

  @Test
  void anExplicitPinMatchesOnlyThatRelease() {
    assertTrue(matches("=25.5.0", "25.5.0"));
    assertTrue(matches("==25.5.0", "25.5.0"));
    assertFalse(matches("=25.5.0", "25.5.1"));
    assertFalse(matches("==25.5.0", "25.4.9"));
  }

  @Test
  void aBareVersionIsAPinToo() {
    assertTrue(matches("25.5.0", "25.5.0"));
    // Missing segments are zero, so the pin still holds across an equivalent spelling.
    assertTrue(matches("25.5", "25.5.0"));
    assertFalse(matches("25.5.0", "25.6.0"));
  }

  @Test
  void aRefusalCanNameWhichBoundFailed() {
    // "too old" and "too new" send an operator in opposite directions, so a verdict is not enough;
    // the install error has to say which end of the window was hit.
    var window = CompatibilityRange.parse(">=25.1, <26.0").orElseThrow();
    assertEquals(List.of("<26.0"), window.failedBounds("26.4"));
    assertEquals(List.of(">=25.1"), window.failedBounds("24.9"));
    assertEquals(List.of(), window.failedBounds("25.5"));
  }

  @Test
  void boundsAreOrderedNumericallyNotAsStrings() {
    assertTrue(matches(">=25.10", "25.10"));
    assertFalse(matches(">=25.10", "25.9"));
    assertFalse(matches("<25.9", "25.10"));
    assertTrue(matches("<25.10", "25.9"));
  }

  @Test
  void aPreReleaseOfTheProductSitsBelowTheReleaseItPrecedes() {
    assertFalse(matches(">=26.0", "26.0-rc1"));
    assertTrue(matches("<26.0", "26.0-rc1"));
  }

  @Test
  void anUnreadableRangeYieldsNoRangeAtAll() {
    // No range at all, never a range that matches everything: silently treating garbage as
    // compatible is how an incompatible jar reaches an instance.
    assertFalse(parses("latest"));
    assertFalse(parses("*"));
    assertFalse(parses(">=next"));
    assertFalse(parses(">=25.x"));
    assertFalse(parses(">=v25.1"));
    assertFalse(parses(">="));
    assertFalse(parses(">=25.1, garbage"));
    assertFalse(parses(">=25.1 <26.0"));
  }

  @Test
  void anAbsentRangeYieldsNoRange() {
    assertFalse(parses(null));
    assertFalse(parses("   "));
    assertFalse(parses(","));
  }

  @Test
  void anUnknownProductVersionMatchesNothing() {
    assertFalse(matches(">=25.1", null));
    assertFalse(matches(">=25.1", "   "));
  }

  @Test
  void aSegmentTooLargeForALongDoesNotBlowUpTheVerdict() {
    // The registry writes these strings; an arithmetic failure here is a 500 on the plugins page.
    assertFalse(matches(">=99999999999999999999.0", "25.2"));
    assertTrue(matches("<99999999999999999999.0", "25.2"));
  }
}
