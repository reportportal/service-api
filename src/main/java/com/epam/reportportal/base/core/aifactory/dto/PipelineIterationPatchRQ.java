package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update of an iteration; absent fields are left as they are. The iteration status is never set
 * directly - it follows from the stages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineIterationPatchRQ {

  @Valid
  @Schema(description = "Attributes to add or replace by key; other attributes are kept")
  private List<PipelineAttribute> attributes;

  @Schema(description = "Keys of attributes to remove")
  private List<String> removeAttributes;

  private Instant startedAt;

  private Instant finishedAt;
}
