package com.epam.reportportal.base.core.tms.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseGenerationMetadataRQ {
  private Integer tokensIn;
  private Integer tokensOut;
  private String model;
  private String skill;
  private BigDecimal costUsd;
}
