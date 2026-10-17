package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.aifactory.model.PipelineLink;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A stage as reported by the pipeline, either with the iteration or on its own. {@code stageKey} is
 * required when the stage arrives with an iteration; when it is reported on its own the key comes from
 * the path.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageRQ {

  @Size(max = 128)
  @Schema(description = "Identifies the stage within the iteration; reporting an existing key updates the stage")
  private String stageKey;

  @Size(max = 256)
  private String name;

  @Schema(description = "Position among stages; defaults to the order of reporting")
  private Integer sequence;

  @Schema(description = "Key of the stage this one is nested in; the parent must be reported in the same request "
      + "or earlier")
  private String parentStageKey;

  @NotBlank
  @Size(max = 64)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Status text, stored exactly as reported")
  private String status;

  @Schema(description = "What the status counts as; derived from well-known status texts when absent, "
      + "UNKNOWN for anything else")
  private StatusCategory statusCategory;

  @Schema(description = "Whether this stage's status counts towards the iteration status (default true); "
      + "a stage that is evaluation only reports false")
  private Boolean affectsIterationStatus;

  private Instant startedAt;

  private Instant finishedAt;

  @Valid
  private List<PipelineAttribute> attributes;

  @Valid
  @Schema(description = "Result tables of the stage")
  private List<PipelineSection> sections;

  @Size(max = 128)
  @Schema(description = "Short text for the stage chip in the iteration card, e.g. \"4 cases\", \"79\" or "
      + "\"4 ↑\". Separate from the attributes, which stay numbers for totals and comparison")
  private String summary;

  @Size(max = 512)
  @Schema(description = "What the stage is doing or waiting for right now, e.g. 'Queued - waiting for runner'")
  private String message;

  @Valid
  @Schema(description = "Links out of the stage; the link to the run is derived from 'execution' and need not be repeated")
  private List<PipelineLink> links;

  @Schema(description = "A document produced by the stage, kept exactly as received. Example: the grader's "
      + "output {\"total_score\": 90, \"cases\": [...]} on a Grade stage")
  private Map<String, Object> document;

  @Size(max = 128)
  @Schema(description = "Name of the schema the document must follow. When ReportPortal has a schema with "
      + "this name the document is checked against it; a document that does not match is rejected and the "
      + "stage is marked Failed with the reason. Example: \"grade\" for the grader's output. "
      + "Leave it out for a document that needs no check")
  private String documentSchemaName;

  private PipelineStepResultRQ result;

  @Valid
  @Schema(description = "Where and how the stage ran, and what is needed to run it again")
  private PipelineExecution execution;
}
