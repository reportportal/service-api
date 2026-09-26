package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsQualityStandardCriterionRQ {

  /**
   * Id of an existing criterion to update in place. {@code null} creates a new
   * criterion; an existing criterion whose id is absent from the request is removed.
   */
  private Long id;

  @NotBlank
  private String name;

  @NotNull
  @Min(1)
  private Integer maxPoints;

  @NotNull
  private Integer sequence;
}
