package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update of an iteration; absent fields are left as they are. The iteration status is never set
 * directly - it follows from the stages (a stage reported TIMED_OUT or INTERRUPTED makes the iteration so).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineIterationPatchRQ {

  @Size(max = 512)
  @Schema(description = "One line outcome of the run, shown on its card. Absent keeps the current value, an empty text clears it. Example: \"Passed · $17.82 · 15.4M tokens\"")
  private String summary;

  @Size(max = 4096)
  @Schema(description = "What the run is for. Absent keeps the current value, an empty text clears it. Example: \"Implement test case · EPMRPP-105554: Invite user modal\"")
  private String description;

  @Size(max = 4096)
  @Schema(description = "Information about the run as a whole that applies to all its stages, as text. "
      + "Absent keeps the current value, an empty text clears it. Example: \"orchestrator $1.81 + 6 stages $16.01\"")
  private String details;

  @Valid
  @Schema(description = "Attributes to add or replace by key; other attributes are kept")
  private List<PipelineAttribute> attributes;

  @Schema(description = "Keys of attributes to remove")
  private List<String> removeAttributes;

  private Instant startedAt;

  private Instant finishedAt;
}
