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
public class TmsTestCaseRS {

  private Long id;

  private String name;

  private String description;

  private String priority;

  private String displayId;

  private Long defaultVersionId;

  private Long createdAt;

  private Long updatedAt;

  private Long lastExecutionAt;

  private TmsTestCaseTestFolderRS testFolder;

  private TmsManualScenarioRS manualScenario;

  private Set<TmsTestCaseAttributeRS> attributes;

  private TmsTestCaseOrigin origin;

  private TmsTestCaseStatus status;

  private TmsTestCaseMetricsRS metrics;

  /**
   * How many review comments of this case are not sent to the agent yet, e.g. 2 for the "2 comments not sent"
   * chip. Absent when there are none.
   */
  private Integer unsentReviewComments;

  /**
   * Whether the case can be sent to automation and where it is in automation. Absent in projects without a
   * pipeline.
   */
  private TmsAutomationStateRS automationState;

  /**
   * Who made the case Ready (a person or Auto-Ready) and when; only for a Ready case that has a recorded
   * promotion.
   */
  private TmsMadeReadyByRS madeReadyBy;
}
