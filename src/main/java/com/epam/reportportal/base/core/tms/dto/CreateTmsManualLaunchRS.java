package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.reporting.ItemAttributeResource;
import com.epam.reportportal.base.reporting.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "TMS Manual Launch creation response")
public class CreateTmsManualLaunchRS {

  @Schema(description = "Launch ID", example = "1")
  private Long id;

  @Schema(description = "Launch name", example = "123")
  private String name;

  @Schema(description = "Launch description", example = "test")
  private String description;

  @Schema(description = "Launch owner", example = "test@test.com")
  private TmsManualLaunchOwnerRS owner;

  @Schema(description = "Launch start time", example = "2025-12-20T23:10:01")
  @JsonProperty("startTime")
  private String startTime;

  @Schema(description = "Launch end time")
  @JsonProperty("endTime")
  private String endTime;

  @Schema(description = "Launch created time", example = "2025-12-20T23:10:01")
  @JsonProperty("createdAt")
  private String createdAt;

  @Schema(description = "Launch number", example = "1")
  private Integer number;

  @JsonProperty("type")
  private String type;

  @JsonProperty("mode")
  private Mode mode;

  @Schema(description = "Launch status", example = "IN_PROGRESS")
  private String status;

  @Schema(description = "Test plan information")
  @JsonProperty("testPlan")
  private TmsManualLaunchTestPlanRS testPlan;

  @Schema(description = "Execution statistics")
  @JsonProperty("executionStatistic")
  private TmsManualLaunchExecutionStatisticRS executionStatistic;

  @Schema(description = "Launch attributes")
  private Set<ItemAttributeResource> attributes;

  @Schema(description = "Launch attributes")
  private BatchTestCaseOperationResultRS addTestCasesStatistic;
}
