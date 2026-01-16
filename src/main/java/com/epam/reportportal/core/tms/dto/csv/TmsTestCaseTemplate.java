package com.epam.reportportal.core.tms.dto.csv;

import java.util.Arrays;
import lombok.Getter;

/**
 * Template types supported for CSV import.
 */
@Getter
public enum TmsTestCaseTemplate {

  TEXT("Text"),
  STEPS("Steps");

  private final String value;

  TmsTestCaseTemplate(String value) {
    this.value = value;
  }

  public static TmsTestCaseTemplate fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return Arrays.stream(values())
        .filter(t -> t.value.equalsIgnoreCase(value.trim()))
        .findFirst()
        .orElse(null);
  }

  public static boolean isValid(String value) {
    return fromString(value) != null;
  }
}
