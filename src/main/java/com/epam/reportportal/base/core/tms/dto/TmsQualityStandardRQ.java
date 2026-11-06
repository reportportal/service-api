package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.core.tms.validation.ValidQualityStandardRQ;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidQualityStandardRQ
public class TmsQualityStandardRQ {

  @NotBlank
  private String name;

  private String description;

  @NotEmpty
  @Valid
  private List<TmsQualityStandardCriterionRQ> criteria;
}
