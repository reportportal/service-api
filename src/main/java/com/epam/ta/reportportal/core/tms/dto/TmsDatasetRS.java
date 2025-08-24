package com.epam.ta.reportportal.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmsDatasetRS {

  private Long id;
  private String name;
  private List<TmsDatasetDataRS> data;
  private List<TmsEnvironmentDatasetRS> environmentDatasets;
  //TODO mask sensitive info ???
}
