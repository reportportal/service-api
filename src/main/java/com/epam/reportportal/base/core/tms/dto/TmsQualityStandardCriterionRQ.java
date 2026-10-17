package com.epam.reportportal.base.core.tms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

  @Schema(description = "Optional stable machine-readable key (lower snake case, unique within the standard); graders may report scores by it instead of the numeric id")
  @Pattern(regexp = "^[a-z][a-z0-9_]{0,63}$", message = "must be lower snake case, up to 64 characters")
  private String key;

  @NotNull
  @Min(1)
  @Max(100)
  private Integer maxPoints;

  @NotNull
  private Integer sequence;
}
