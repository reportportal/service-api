package com.epam.ta.reportportal.core.tms.dto;

import lombok.Getter;

@Getter
public enum TmsTestCaseExportFormat {
  JSON("json"),
  CSV("csv");

  private final String value;

  TmsTestCaseExportFormat(String value) {
    this.value = value;
  }

  public static TmsTestCaseExportFormat fromString(String value) {
    for (TmsTestCaseExportFormat format : values()) {
      if (format.value.equalsIgnoreCase(value)) {
        return format;
      }
    }
    throw new IllegalArgumentException("Unsupported export format: " + value);
  }
}
