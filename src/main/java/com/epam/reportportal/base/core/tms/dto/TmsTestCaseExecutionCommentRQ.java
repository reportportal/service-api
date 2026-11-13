package com.epam.reportportal.base.core.tms.dto;

import static com.epam.reportportal.base.reporting.ValidationConstraints.MAX_TMS_TEST_CASE_EXECUTION_COMMENT_LENGTH;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestCaseExecutionCommentRQ {

  @Size(max = MAX_TMS_TEST_CASE_EXECUTION_COMMENT_LENGTH)
  private String comment;
  
  private List<TmsTestCaseExecutionCommentAttachmentRQ> attachments;
}
