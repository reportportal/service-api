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
   * {@code 2.0.0}. Two suffixes on the same numbers are ordered by semver §11.4 — identifier by
   * identifier, numeric ones as numbers, numeric below alphanumeric, shorter below longer — so
   * {@code 2.0.0-rc.10} is above {@code 2.0.0-rc.2} rather than below it.
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

  /**
   * Pre-release precedence, per semver §11.4: identifier by identifier, numeric ones compared as
   * numbers, a numeric identifier below an alphanumeric one, and a shorter run of otherwise equal
   * identifiers below a longer one.
   *
   * <p>This used to compare the whole suffix as one string, which ranked {@code 2.0.0-rc.10} below
   * {@code 2.0.0-rc.2} because {@code '1'} sorts before {@code '2'}. That is not a cosmetic
   * ordering complaint: {@code updateFor} offers an update only when the latest version compares
   * above the installed one, so a plugin on its tenth release candidate was told it was already
   * ahead of the newest build and the update was never offered.
   *
   * @param left  the suffix including its leading {@code -}, or empty for a release
   * @param right the other one, same shape
   */
  private static int comparePreRelease(String left, String right) {
    if (left.equals(right)) {
      return 0;
    }
    // having a pre-release at all is what lowers a version below the release it precedes
    if (left.isEmpty()) {
      return 1;
    }
    if (right.isEmpty()) {
      return -1;
    }

    var leftParts = identifiers(left);
    var rightParts = identifiers(right);
    for (var i = 0; i < Math.min(leftParts.length, rightParts.length); i++) {
      var result = compareIdentifier(leftParts[i], rightParts[i]);
      if (result != 0) {
        return result;
      }
    }

    return Integer.compare(leftParts.length, rightParts.length);
  }

  /** The dot-separated identifiers of a suffix, with the {@code -} that introduces it removed. */
  private static String[] identifiers(String preRelease) {
    return StringUtils.removeStart(preRelease, "-").split("\\.");
  }

  private static int compareIdentifier(String left, String right) {
    var leftNumeric = isNumeric(left);
    var rightNumeric = isNumeric(right);
    if (leftNumeric && rightNumeric) {
      // through compareDigits rather than by parsing: an identifier the registry chose may be
      // longer than a long, and throwing here would take the plugins page down with it
      return compareDigits(left, right);
    }
    if (leftNumeric != rightNumeric) {
      return leftNumeric ? -1 : 1;
    }

    return left.compareTo(right);
  }

  /** Numeric in the semver sense: digits only, and at least one of them. */
  private static boolean isNumeric(String identifier) {
    return !identifier.isEmpty() && identifier.chars().allMatch(Character::isDigit);
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
