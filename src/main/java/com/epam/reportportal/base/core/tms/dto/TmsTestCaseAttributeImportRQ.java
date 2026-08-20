package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsTestCaseAttributeImportRQ {

  @Size(max = 512)
  private String key;
}