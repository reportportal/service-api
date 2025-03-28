package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsProductVersionRS {

  private Long id;
  private String version;
  private String documentation;
  private Long projectId;
}
