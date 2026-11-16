package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanProductVersionRS {

  private Long id;

  private String version;
}
