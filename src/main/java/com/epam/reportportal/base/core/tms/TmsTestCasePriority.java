package com.epam.reportportal.base.core.tms;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

/**
 * Canonical priorities supported by the Test Case Library.
 */
public enum TmsTestCasePriority {

  BLOCKER,
  CRITICAL,
  HIGH,
  MEDIUM,
  LOW,
  UNSPECIFIED;

  /**
   * Converts a source priority to a canonical TMS priority.
   *
   * <p>The conversion is case-insensitive and supports QA Space priority names. Empty and
   * unrecognized values are represented as {@link #UNSPECIFIED}.</p>
   *
   * @param priority priority value received from a client or an external system
   * @return canonical uppercase TMS priority
   */
  public static String normalize(String priority) {
    if (StringUtils.isBlank(priority)) {
      return UNSPECIFIED.name();
    }

    return switch (priority.trim().toUpperCase(Locale.ROOT)) {
      case "BLOCKER" -> BLOCKER.name();
      case "CRITICAL" -> CRITICAL.name();
      case "MAJOR", "HIGH" -> HIGH.name();
      case "MEDIUM" -> MEDIUM.name();
      case "MINOR", "TRIVIAL", "LOW" -> LOW.name();
      case "UNSPECIFIED" -> UNSPECIFIED.name();
      default -> UNSPECIFIED.name();
    };
  }
}
