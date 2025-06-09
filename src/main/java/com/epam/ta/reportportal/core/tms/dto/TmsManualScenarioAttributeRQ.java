package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for manual scenario attribute operations.
 * Contains information about an attribute of the manual scenario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsManualScenarioAttributeRQ {

  /**
   * Value of the attribute.
   * Must not be blank.
   */
  @NotBlank(message = "Attribute value must not be blank")
  private String value;

  /**
   * ID of the attribute.
   * Must not be null.
   */
  @NotNull(message = "Attribute ID must not be null")
  private Long attributeId;
}
