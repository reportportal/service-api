package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestCaseInTestPlanRS {

  private Long id;

  private String name;

  private String description;

  private String priority;

  private String externalId;

  private Long createdAt;

  private Long updatedAt;

  private TmsTestCaseTestFolderRS testFolder;

  private TmsManualScenarioRS manualScenario;

  private Set<TmsTestCaseAttributeRS> attributes;

  private TmsTestCaseExecutionInTestPlanRS lastExecution;

  private Set<TmsTestCaseExecutionInTestPlanRS> executions;
}
