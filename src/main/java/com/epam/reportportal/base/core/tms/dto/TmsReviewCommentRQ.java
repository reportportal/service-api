package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A new review comment on an AI test case.
 * <p>
 * Example: {@code {"targetType": "STEP", "stepNumber": 2, "text": "Name the exact toast text"}} comments step 2;
 * {@code {"targetType": "PRECONDITION", "text": "Split this case into two"}} comments the precondition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsReviewCommentRQ {

  @NotNull
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Where the comment is attached: PRECONDITION, STEP or SCENARIO_TEXT (the instructions / "
          + "expected result block of a Text case)")
  private TmsReviewCommentTarget targetType;

  @Min(1)
  @Schema(description = "1-based position of the commented step; required for STEP and not allowed otherwise")
  private Integer stepNumber;

  @NotBlank
  @Size(max = 4000)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "What the agent should change")
  private String text;
}
