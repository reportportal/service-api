package com.epam.ta.reportportal.core.tms.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseDefaultVersionTestCaseId {

  private TmsTestCaseVersion testCaseVersion;

  private Long testCaseId;
}
