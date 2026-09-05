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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * The {@code compatibility.reportportal} range a plugin version declares — comma-separated
 * constraints intersected, e.g. {@code >=25.1} or {@code >=25.1, <26.0}.
 *
 * <p>A range that cannot be parsed yields no {@link CompatibilityRange} at all rather than one
 * that matches everything: an unreadable claim of compatibility is not a claim of compatibility.
 */
public final class CompatibilityRange {

  private final List<Constraint> constraints;

  private CompatibilityRange(List<Constraint> constraints) {
    this.constraints = constraints;
  }

  /**
   * Parses a range.
   *
   * @param range the declared range, may be null or blank
   * @return the range, or empty when it is absent, blank or not understood
   */
  public static Optional<CompatibilityRange> parse(String range) {
    var trimmed = StringUtils.trimToNull(range);
    if (trimmed == null) {
      return Optional.empty();
    }
    var constraints = new ArrayList<Constraint>();
    for (var part : trimmed.split(",")) {
      var constraint = Constraint.parse(part);
      if (constraint == null) {
        return Optional.empty();
      }
      constraints.add(constraint);
    }
    return constraints.isEmpty() ? Optional.empty()
        : Optional.of(new CompatibilityRange(constraints));
  }

  /**
   * Whether a product version satisfies every constraint.
   *
   * @param productVersion the running ReportPortal version
   * @return true when all constraints hold
   */
  public boolean matches(String productVersion) {
    if (StringUtils.isBlank(productVersion)) {
      return false;
    }
    return constraints.stream().allMatch(constraint -> constraint.matches(productVersion));
  }

  /**
   * The bounds a product version fails, in the order they were declared.
   *
   * <p>An install refusal has to name which bound failed — "too old" and "too new" send an operator
   * in opposite directions, and {@link #matches} cannot tell them apart.
   *
   * @param productVersion the running ReportPortal version
   * @return the unsatisfied constraints as written, empty when the range matches
   */
  public List<String> failedBounds(String productVersion) {
    return constraints.stream()
        .filter(constraint -> !constraint.matches(productVersion))
        .map(Constraint::text)
        .toList();
  }

  private record Constraint(String operator, String version) {

    private static final List<String> OPERATORS = List.of(">=", "<=", "==", ">", "<", "=");

    /**
     * A dotted number, optionally pre-release, optionally with build metadata. Anything else is
     * not a version: {@code >=25.x} would otherwise compare as {@code >=25.0} and {@code >=next}
     * as {@code >=0}, i.e. every unreadable bound would silently mean "everything is compatible".
     *
     * <p>Possessive throughout. This string arrives in a manifest the registry serves, so it is
     * not ours; the greedy form nests a repetition inside a repetition, and Java's engine
     * backtracks through that recursively — a long enough run of digits and dots takes the
     * thread down with a StackOverflowError rather than returning no match. Possessive
     * quantifiers do not backtrack at all, which removes the recursion instead of bounding it.
     */
    private static final Pattern VERSION_PATTERN =
        Pattern.compile("\\d++(\\.\\d++)*+(-[0-9A-Za-z.-]++)?+(\\+[0-9A-Za-z.-]++)?+");

    /**
     * Longer than any real version, and short enough that nothing downstream has to think about
     * size. A bound as well as the possessive form: the two answer different questions, one how
     * much work a match may cost and the other what a version may plausibly be.
     */
    private static final int MAX_VERSION_LENGTH = 256;

    static Constraint parse(String text) {
      var trimmed = StringUtils.trimToEmpty(text);
      for (var operator : OPERATORS) {
        if (trimmed.startsWith(operator)) {
          return of(operator, StringUtils.trimToEmpty(trimmed.substring(operator.length())));
        }
      }
      // A bare version is an exact pin, as in the plan's '=25.5.0' row without the sign.
      return of("=", trimmed);
    }

    private static Constraint of(String operator, String version) {
      if (version.length() > MAX_VERSION_LENGTH) {
        return null;
      }
      return VERSION_PATTERN.matcher(version).matches() ? new Constraint(operator, version) : null;
    }

    String text() {
      return operator + version;
    }

    boolean matches(String productVersion) {
      var comparison = PluginVersions.compare(productVersion, version);
      return switch (operator) {
        case ">=" -> comparison >= 0;
        case "<=" -> comparison <= 0;
        case ">" -> comparison > 0;
        case "<" -> comparison < 0;
        default -> comparison == 0;
      };
    }
  }
}
