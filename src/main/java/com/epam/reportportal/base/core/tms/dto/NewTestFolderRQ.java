package com.epam.reportportal.base.core.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewTestFolderRQ {

  private String name;

  private Long parentTestFolderId;

  private Integer index;
}
