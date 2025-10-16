package com.epam.ta.reportportal.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmsManualLaunchRQ {

  private String name;

  private String description;

  private List<Long> testCaseIds;

  private List<TmsManualLaunchAttributeRQ> attributes;
}
