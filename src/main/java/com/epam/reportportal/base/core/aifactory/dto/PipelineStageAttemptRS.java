package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineLink;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineStageAttemptRS {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "1-based attempt number")
  private Integer attempt;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String status;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private StatusCategory statusCategory;
  private Instant startedAt;
  private Instant finishedAt;
  private Long durationMillis;
  private List<PipelineLink> links;
  private PipelineStepResultRS result;
  private List<PipelineAttribute> attributes;
  private List<PipelineSection> sections;
}
