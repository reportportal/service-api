package com.epam.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for manual scenario attribute operations. Contains information about an attribute of
 * the manual scenario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsManualScenarioAttributeRQ {

  private Long id;

  private String value;
}
