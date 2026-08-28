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

  private record Constraint(String operator, String version) {

    private static final List<String> OPERATORS = List.of(">=", "<=", "==", ">", "<", "=");

    static Constraint parse(String text) {
      var trimmed = StringUtils.trimToEmpty(text);
      for (var operator : OPERATORS) {
        if (trimmed.startsWith(operator)) {
          var version = StringUtils.trimToNull(trimmed.substring(operator.length()));
          return version == null ? null : new Constraint(operator, version);
        }
      }
      // A bare version is an exact pin, as in the plan's '=25.5.0' row without the sign.
      return trimmed.isEmpty() || !Character.isDigit(trimmed.charAt(0)) ? null
          : new Constraint("=", trimmed);
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
