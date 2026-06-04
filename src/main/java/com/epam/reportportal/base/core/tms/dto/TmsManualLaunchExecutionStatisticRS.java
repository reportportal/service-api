package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Execution statistics for manual launch")
public class TmsManualLaunchExecutionStatisticRS {

  @Schema(description = "Total test cases count", example = "1231")
  private int total;

  @Schema(description = "Failed test cases count", example = "12")
  private int failed;

  @Schema(description = "Passed test cases count", example = "45")
  private int passed;

  @Schema(description = "To run test cases count", example = "332")
  @JsonProperty("toRun")
  private int toRun;

  @Schema(description = "In progress test cases count", example = "232")
  @JsonProperty("inProgress")
  private int inProgress;

  @Schema(description = "Skipped test cases count", example = "123")
  private int skipped;
}
