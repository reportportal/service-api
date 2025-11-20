package com.epam.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsEnvironmentDatasetRQ {

  private String environmentId;
  private TmsDatasetType datasetType;
}
