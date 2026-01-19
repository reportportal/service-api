package com.epam.reportportal.core.tms.dto.csv;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single parsed CSV row with all relevant fields for TMS test case import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseCsvRow {

  private int rowNumber;

  // Core identification
  private String template;
  private String summary;
  private String description;

  // Meta fields
  private String priority;
  private List<String> labels;
  private String requirements;

  // Folder/path
  private String path;

  // Scenario content
  private String testSteps;
  private String expectedResult;

  // Ignored fields (stored for potential future use)
  private String status;
  private String testType;
  private String components;
  private String versions;
  private String bugs;
}
