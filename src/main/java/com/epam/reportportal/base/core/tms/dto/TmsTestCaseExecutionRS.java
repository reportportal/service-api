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
public class TmsTestCaseExecutionRS {

  private Long id;

  private String executionStatus;

  private TmsTestCaseExecutionCommentRS executionComment;

  private Long startedAt;

  private Long finishedAt;

  private Long duration;

  private Long testCaseVersionId;

  private Long testItemId;

  private Long testCaseId;

  private String testCaseName;

  private String testCaseDescription;

  private String testCasePriority;

  private TmsTestCaseExecutionTestFolderRS testFolder;

  private TmsManualScenarioRS manualScenario;

  private Set<TmsTestCaseExecutionAttributeRS> attributes;
}
