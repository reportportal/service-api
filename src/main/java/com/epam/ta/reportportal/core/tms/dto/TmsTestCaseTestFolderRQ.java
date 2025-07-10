package com.epam.ta.reportportal.core.tms.dto;

import com.epam.ta.reportportal.core.tms.validation.ValidTestFolderIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidTestFolderIdentifier
public class TmsTestCaseTestFolderRQ {

  private Long id;

  private String name;

}
