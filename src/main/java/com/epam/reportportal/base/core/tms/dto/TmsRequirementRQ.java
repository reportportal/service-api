package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsRequirementRQ {

  @Schema(description = "Omit for a new requirement, the server assigns a UUID. Send the id only "
      + "for an existing requirement of this test case")
  @Size(max = 255)
  private String id;

  @NotBlank
  @Size(max = 255)
  private String value;
}
