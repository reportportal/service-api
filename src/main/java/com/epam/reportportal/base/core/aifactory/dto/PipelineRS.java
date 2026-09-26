package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineRS {
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Long id;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;
  private String type;
  private String description;
  @Schema(description = "Typed facts about the pipeline itself, e.g. its repository")
  private List<PipelineAttribute> attributes;
  @Schema(description = "Settings read by the behaviour plugged into the pipeline, e.g. {\"autoReady\": {\"enabled\": true, \"threshold\": 90}}")
  private Map<String, Object> settings;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private long iterationsCount;
  private PipelineIterationSummaryRS latestIteration;
  private Instant createdAt;
}
