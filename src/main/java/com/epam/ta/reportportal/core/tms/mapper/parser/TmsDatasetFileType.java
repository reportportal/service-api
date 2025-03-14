package com.epam.ta.reportportal.core.tms.mapper.parser;

public enum TmsDatasetFileType {
  CSV,
  XLSX,

  UNKNOWN;

  /**
   * Returns the corresponding TmsDatasetFileType enum value for the given string, ignoring case.
   *
   * @param value the string representing the enum value
   * @return the matching TmsDatasetFileType, or null if no match is found
   * @throws IllegalArgumentException if the input value is null or no match is found
   */
  public static TmsDatasetFileType fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Input value cannot be null");
    }

    for (TmsDatasetFileType type : TmsDatasetFileType.values()) {
      if (type.name().equalsIgnoreCase(value)) {
        return type;
      }
    }

    return UNKNOWN;
  }

}
