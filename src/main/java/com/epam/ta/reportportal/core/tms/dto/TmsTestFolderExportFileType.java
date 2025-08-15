package com.epam.ta.reportportal.core.tms.dto;

public enum TmsTestFolderExportFileType {
  CSV;

  /**
   * Returns the corresponding TmsTestFolderExportFileType enum value for the given string, ignoring
   * case.
   *
   * @param value the string representing the enum value
   * @return the matching TmsTestFolderExportFileType, or null if no match is found
   * @throws IllegalArgumentException if the input value is null or no match is found
   */
  public static TmsTestFolderExportFileType fromString(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Input value cannot be null");
    }

    for (TmsTestFolderExportFileType type : TmsTestFolderExportFileType.values()) {
      if (type.name().equalsIgnoreCase(value)) {
        return type;
      }
    }

    throw new IllegalArgumentException("Unknown value");
  }

}
