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

import org.apache.commons.lang3.StringUtils;

/**
 * Ordering for dotted version numbers — plugin semver and the CalVer of the product alike.
 */
public final class PluginVersions {

  private PluginVersions() {
    // utility
  }

  /**
   * Compares two dotted versions numerically, segment by segment. Missing segments count as zero,
   * so {@code 1.4} and {@code 1.4.0} are equal, and {@code 1.4.10} is above {@code 1.4.9} where a
   * string comparison would put it below.
   *
   * <p>A pre-release suffix sorts below the release it precedes: {@code 2.0.0-rc1} is below
   * {@code 2.0.0}. Two different suffixes on the same numbers are ordered lexically, which is a
   * guess, but the alternative is calling them equal and silently offering no update.
   *
   * <p>Build metadata carries no precedence, as semver says: {@code 2.0.0+build.5} is
   * {@code 2.0.0}. Ordering on it would rank a release candidate above the release itself, since
   * {@code +} sorts below {@code -}.
   *
   * @param left  a version, may be null
   * @param right a version, may be null
   * @return negative, zero or positive as left is below, equal to or above right; a null version
   *     sorts below any non-null one
   */
  public static int compare(String left, String right) {
    if (left == null || right == null) {
      return left == null ? (right == null ? 0 : -1) : 1;
    }
    var numbers = compareNumbers(core(left), core(right));
    if (numbers != 0) {
      return numbers;
    }
    return comparePreRelease(preRelease(left), preRelease(right));
  }

  private static int compareNumbers(String left, String right) {
    var leftParts = left.split("\\.");
    var rightParts = right.split("\\.");
    var length = Math.max(leftParts.length, rightParts.length);
    for (var i = 0; i < length; i++) {
      var result = compareDigits(segment(leftParts, i), segment(rightParts, i));
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  /**
   * Numeric order over digit strings, without parsing them. The registry chooses these strings and
   * a segment longer than a {@code long} would otherwise throw out of the plugins page.
   */
  private static int compareDigits(String left, String right) {
    var trimmedLeft = stripLeadingZeroes(left);
    var trimmedRight = stripLeadingZeroes(right);
    return trimmedLeft.length() == trimmedRight.length()
        ? trimmedLeft.compareTo(trimmedRight)
        : Integer.compare(trimmedLeft.length(), trimmedRight.length());
  }

  private static String stripLeadingZeroes(String digits) {
    var first = 0;
    while (first < digits.length() - 1 && digits.charAt(first) == '0') {
      first++;
    }
    return digits.substring(first);
  }

  private static int comparePreRelease(String left, String right) {
    if (left.equals(right)) {
      return 0;
    }
    if (left.isEmpty()) {
      return 1;
    }
    if (right.isEmpty()) {
      return -1;
    }
    return left.compareTo(right);
  }

  private static String segment(String[] parts, int index) {
    if (index >= parts.length) {
      return "0";
    }
    var digits = parts[index].chars().takeWhile(Character::isDigit)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
    return digits.isEmpty() ? "0" : digits;
  }

  /** The version with its build metadata dropped: that part has no precedence. */
  private static String withoutBuildMetadata(String version) {
    var trimmed = StringUtils.trimToEmpty(version);
    var plus = trimmed.indexOf('+');
    return plus < 0 ? trimmed : trimmed.substring(0, plus);
  }

  private static String core(String version) {
    var significant = withoutBuildMetadata(version);
    var dash = significant.indexOf('-');
    return dash < 0 ? significant : significant.substring(0, dash);
  }

  private static String preRelease(String version) {
    var significant = withoutBuildMetadata(version);
    return significant.substring(core(version).length());
  }
}
