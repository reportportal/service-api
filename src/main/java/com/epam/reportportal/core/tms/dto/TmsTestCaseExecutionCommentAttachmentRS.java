package com.epam.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsTestCaseExecutionCommentAttachmentRS {

  @NotBlank(message = "Attachment ID must not be blank")
  private Long id;

  private String fileName;

  private String fileType;

  private long fileSize;
}
