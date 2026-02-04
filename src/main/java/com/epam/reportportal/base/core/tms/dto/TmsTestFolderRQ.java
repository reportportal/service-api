package com.epam.reportportal.base.core.tms.dto;

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
  private Long parentTestFolderId;
  private NewTestFolderRQ parentTestFolder;
}
