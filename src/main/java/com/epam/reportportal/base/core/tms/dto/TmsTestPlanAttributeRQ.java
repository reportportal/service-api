package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanAttributeRQ {

  private Long id;

  @Size(max = 512)
  private String key;

  @Size(max = 512)
  private String value;
}