package com.epam.reportportal.core.tms.dto;

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

  private Long testCaseVersionId;

  private Long testItemId;

  private Long testCaseId;

  private String status;

  private TmsTestCaseExecutionCommentRS executionComment;

  private String name;

  private String description;

  private String priority;

  private Long createdAt;

  private Long updatedAt;

  private TmsTestCaseTestFolderRS testFolder;

  private TmsManualScenarioRS manualScenario;

  private Set<TmsTestCaseExecutionAttributeRS> attributes;
}
