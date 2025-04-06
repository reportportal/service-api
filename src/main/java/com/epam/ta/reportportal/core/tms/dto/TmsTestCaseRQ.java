package com.epam.ta.reportportal.core.tms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseRQ {

  @NotEmpty
  private String name;

  private String description;

  @NotNull
  private Long testFolderId;

  private List<TmsTestCaseAttributeRQ> tags;

  private Long datasetId;
}
