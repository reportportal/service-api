package com.epam.ta.reportportal.core.tms.dto;

import lombok.Getter;

@Getter
public enum TmsTestCaseImportFormat {
  JSON("json"),
  CSV("csv");

  private final String value;

  TmsTestCaseImportFormat(String value) {
    this.value = value;
  }

  public static TmsTestCaseImportFormat fromString(String value) {
    for (TmsTestCaseImportFormat format : values()) {
      if (format.value.equalsIgnoreCase(value)) {
        return format;
      }
    }
    throw new IllegalArgumentException("Unsupported import format: " + value);
  }

  public static TmsTestCaseImportFormat fromFileName(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      throw new IllegalArgumentException("Invalid file name: " + fileName);
    }
    var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
    return fromString(extension);
  }
}
