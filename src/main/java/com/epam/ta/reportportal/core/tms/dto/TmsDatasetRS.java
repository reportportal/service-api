package com.epam.ta.reportportal.core.tms.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TmsDatasetRS {

  private Long id;
  private String name;
  private List<TmsDatasetDataRS> data;
  //TODO mask sensitive info ???
}
