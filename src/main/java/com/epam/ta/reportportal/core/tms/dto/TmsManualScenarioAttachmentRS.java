package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for manual scenario attachment operations.
 * Contains information about an attachment in a manual scenario or step.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsManualScenarioAttachmentRS {

  /**
   * ID of the attachment.
   * Must not be blank.
   */
  @NotBlank(message = "Attachment ID must not be blank")
  private String id;
}
