package com.epam.ta.reportportal.core.tms.dto;

import com.epam.ta.reportportal.entity.tms.TmsTestPlanExecutionStatisticRS;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanRS {

  private Long id;
  private String name;
  private String description;
  private TmsTestPlanExecutionStatisticRS executionStatistic;
  private List<TmsTestPlanAttributeRS> attributes;
}
