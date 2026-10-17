package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Which pipeline an iteration belongs to, and what is known about the pipeline itself. The pipeline is
 * created by the first report that names it.
 * <p>
 * Example: {@code {"name": "Test case generation", "type": "TEST_CASE_GENERATION",
 * "attributes": [{"key": "repository", "value": "EPM-RPP/rp-tests", "type": "META"}]}}. The attributes
 * describe the pipeline (its repository); the iteration's own attributes (requirement, model, environment)
 * are sent next to this object, not inside it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineDefinitionRQ {

  @NotBlank
  @Size(max = 256)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Unique within the project; the pipeline "
      + "is created by the first report that names it")
  private String name;

  @Size(max = 64)
  @Schema(description = "Kind of the pipeline (e.g. TEST_CASE_GENERATION); recorded when the pipeline is first "
      + "reported and selects the behaviour plugged into that kind. Reporting a different type for an "
      + "existing pipeline is rejected")
  private String type;

  @Valid
  @Schema(description = "Typed facts about the pipeline itself (e.g. its repository); added to or replacing the "
      + "pipeline's attributes by key")
  private List<PipelineAttribute> attributes;

  public static PipelineDefinitionRQ named(String name) {
    return PipelineDefinitionRQ.builder().name(name).build();
  }
}
