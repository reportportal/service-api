package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestFolderRQ {

  private String name;
  private String description;
  private ParentTmsTestFolderRQ parentTestFolder;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public static class ParentTmsTestFolderRQ {

    private Long id;
    private String name;
  }
}
