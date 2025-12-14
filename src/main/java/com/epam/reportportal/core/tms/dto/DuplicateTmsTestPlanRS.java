package com.epam.reportportal.core.tms.dto;

import com.epam.reportportal.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DuplicateTmsTestPlanRS {

  private Long id;
  private String name;
  private String description;
  private Long milestoneId;
  private TmsTestPlanExecutionStatisticRS executionStatistic;
  private List<TmsTestPlanAttributeRS> attributes;
  private BatchTestCaseOperationResultRS duplicationStatistic;
}
