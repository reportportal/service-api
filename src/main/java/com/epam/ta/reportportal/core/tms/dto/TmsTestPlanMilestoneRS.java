package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanMilestoneRS {

  private Long id;
  private String name;
  private String type;
  private String startDate;
  private String endDate;
  private TmsTestPlanProductVersionRS productVersion;
}
